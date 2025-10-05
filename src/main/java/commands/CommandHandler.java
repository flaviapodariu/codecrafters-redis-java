package commands;

import commands.async.*;
import commands.strategies.lists.*;
import commands.strategies.misc.*;
import commands.strategies.streams.XADDStrategy;
import commands.strategies.streams.XRANGEStrategy;
import commands.strategies.streams.XREADStrategy;
import commands.strategies.strings.GETStrategy;
import commands.strategies.strings.INCRStrategy;
import commands.strategies.strings.SETStrategy;
import commands.strategies.transactional.DISCARDStrategy;
import commands.strategies.transactional.EXECStrategy;
import commands.strategies.transactional.MULTIStrategy;
import commands.transaction.TransactionManager;
import commands.transaction.TransactionalClientManager;
import commands.transaction.TransactionalCommandStrategy;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import server.Configuration;
import store.KeyValueStore;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static commands.Command.*;
import static commands.ProtocolUtils.*;
import static java.util.Map.entry;

@Slf4j
@Getter
@Setter
public class CommandHandler implements BlockingClientManager, TransactionManager {

    // todo strategies should be final. fix the blop issue when constructing the map
    private Map<Command, Object> strategies;
    private final AsyncCommandObserver asyncCommandObserver;

    private final Map<String, List<BlockedClient>> waitingClients = new ConcurrentHashMap<>();
    private final TransactionalClientManager clientManager = new TransactionalClientManager();

    public CommandHandler(KeyValueStore kvStore, AsyncCommandObserver observer, Configuration configuration) {
        this.strategies = new HashMap<>(Map.ofEntries(
                entry(COMMAND, new DOCSStrategy()),
                entry(PING, new PINGStrategy()),
                entry(ECHO, new ECHOStrategy()),
                entry(GET, new GETStrategy(kvStore)),
                entry(SET, new SETStrategy(kvStore)),
                entry(DEL, new DELStrategy(kvStore)),
                entry(LRANGE, new LRANGEStrategy(kvStore)),
                entry(LLEN, new LLENStrategy(kvStore)),
                entry(LPOP, new LPOPStrategy(kvStore)),
                entry(TYPE, new TYPEStrategy(kvStore)),
                entry(XRANGE, new XRANGEStrategy(kvStore)),
                entry(INCR, new INCRStrategy(kvStore)),
                entry(MULTI, new MULTIStrategy(clientManager)),
                entry(DISCARD, new DISCARDStrategy(clientManager)),
                entry(INFO, new INFOStrategy(configuration))
        ));

        strategies.put(BLPOP, new BLPOPStrategy(kvStore, this));
        strategies.put(RPUSH, new RPUSHStrategy(kvStore, this));
        strategies.put(LPUSH, new LPUSHStrategy(kvStore, this));
        strategies.put(XREAD, new XREADStrategy(kvStore, this));
        strategies.put(XADD, new XADDStrategy(kvStore, this));
        strategies.put(EXEC, new EXECStrategy(clientManager, this));
        this.asyncCommandObserver = observer;
    }

    public void execute(List<String> args, SocketChannel clientSocket) {
        var command = args.getFirst().toUpperCase();

        var strategy = strategies.getOrDefault(fromString(command), null);

        if (clientManager.isInTransaction(clientSocket)) {
            if (!(strategy instanceof EXECStrategy || strategy instanceof DISCARDStrategy)) {
                clientManager.queueCommand(args, clientSocket);
                asyncCommandObserver.onResponseReady(clientSocket, ByteBuffer.wrap(QUEUED.getBytes()));
                return;
            }
        }

        switch (strategy) {
            case null -> {
                String error = String.format("Command %s does not exist", command);
                asyncCommandObserver.onResponseReady(clientSocket, ByteBuffer.wrap(encodeSimpleError(error).getBytes()));
            }
            case CommandStrategy syncCommand -> {
                var response = syncCommand.execute(args.subList(1, args.size()));
                asyncCommandObserver.onResponseReady(clientSocket, response);
            }
            case AsyncCommandStrategy asyncCommand ->
                    asyncCommand.executeAsync(args.subList(1, args.size()), clientSocket);
            case TransactionalCommandStrategy transactionalCommand -> {
                var response = transactionalCommand.execute(args.subList(1, args.size()), clientSocket);
                asyncCommandObserver.onResponseReady(clientSocket, response);
            }
            default -> {
            }
        }
    }

    /**
     * Method forces async commands to behave like synchronous commands by returning instantly,
     * whether they have a response prepared or not
     * @param args the args to call the command inside this transaction
     * @param clientSocket the client socket channel involved in this transaction
     * @return the command's response
     */
    public ByteBuffer executeInTransaction(List<String> args, SocketChannel clientSocket) {
        var command = args.getFirst().toUpperCase();

        var strategy = strategies.getOrDefault(fromString(command), null);

        if (strategy instanceof CommandStrategy cs) {
            return cs.execute(args.subList(1, args.size()));
        }

        if (strategy instanceof TransactionalCommandStrategy tcs) {
            return tcs.execute(args.subList(1, args.size()), clientSocket);
        }
        // todo what happens here?
        return null;
    }


    /**
     * Flags a client as waiting
     * @param key the key for which the client is waiting
     * @param blockedClient the blocked client
     */
    @Override
    public void registerBlockingClient(String key, BlockedClient blockedClient) {
        this.waitingClients.computeIfAbsent(key, _ -> new LinkedList<>());
        this.waitingClients.get(key).add(blockedClient);
    }

    @Override
    public void unblockClient(String key, Command waitingFor, UnblockingMethod method) {
        var waitingClients = this.waitingClients.get(key);
        if (waitingClients != null && !waitingClients.isEmpty()) {
            log.debug("get client to unblock");
            var unblockedClient = this.waitingClients.get(key).removeFirst();

            if (unblockedClient != null) {
                executeUnblockingCommand(key, waitingFor, unblockedClient);
            }
        }
    }

    @Override
    public void updateStreamIdForBlockedClient(String key, String lastStreamId) {
        if (this.waitingClients.get(key) != null) {
            var blockedClients = this.waitingClients.get(key);
            blockedClients.forEach(client -> {
                if (client.getIds().getFirst().equals("$")) {
                    client.setIds(List.of(lastStreamId));
                }
            });
        }
    }

    private void executeUnblockingCommand(String key, Command waitingFor, BlockedClient client) {
        var channel = client.getChannel();

        switch (waitingFor) {
            case LPOP -> {
                var pop = (LPOPStrategy) this.strategies.get(LPOP);
                var removedItem = pop.execute(key);
                if (removedItem != null) {
                    var response = Arrays.asList(key, removedItem);
                    sendResponse(
                            channel,
                            ByteBuffer.wrap(
                                    ProtocolUtils.encode(response).getBytes())
                    );
                }
            }
            case XREAD -> {
                var readStream = (XREADStrategy) this.strategies.get(XREAD);
                // todo implement count
                ByteBuffer response;
                response = readStream.execute(client.getKeys(), client.getIds(), 1);
                sendResponse(channel, response);
            }
            case NO_COMMAND ->
                sendResponse(
                        channel,
                        // TODO what if other command wants to return something else?
                        ByteBuffer.wrap(
                                ProtocolUtils.encode(NULL_LIST).getBytes())
                        );
            default -> throw new IllegalStateException("Unexpected value: " + waitingFor);
        }
    }

    @Override
    public void sendResponse(SocketChannel channel, ByteBuffer response) {
        asyncCommandObserver.onResponseReady(channel, response);
    }

    @Override
    public ByteBuffer onExecuteTransaction(List<List<String>> commandList, SocketChannel channel) {
        var transactionResult = new ArrayList<ByteBuffer>();
        commandList.forEach(command -> {
                var commandResult = executeInTransaction(command, channel);
                transactionResult.add(commandResult);
        });

        return encodeTransaction(transactionResult);
    }
}