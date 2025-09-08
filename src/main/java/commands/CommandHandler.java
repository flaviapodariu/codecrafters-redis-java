package commands;

import commands.async.*;
import commands.strategies.*;
import lombok.Getter;
import lombok.Setter;
import store.KeyValueStore;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static commands.Command.*;
import static commands.ProtocolUtils.NULL_LIST;
import static java.util.Map.entry;

@Getter
@Setter
public class CommandHandler implements BlockingClientManager {

    // todo strategies should be final. fix the blop issue when constructing the map
    private Map<Command, Object> strategies;
    private final AsyncCommandObserver asyncCommandObserver;

    private final Map<String, List<BlockedClient>> waitingClients = new ConcurrentHashMap<>();

    public CommandHandler(KeyValueStore kvStore, AsyncCommandObserver observer) {
        this.strategies = new HashMap<>(Map.ofEntries(
                entry(COMMAND, new DOCSStrategy()),
                entry(PING, new PINGStrategy()),
                entry(ECHO, new ECHOStrategy()),
                entry(GET, new GETStrategy(kvStore)),
                entry(SET, new SETStrategy(kvStore)),
                entry(LRANGE, new LRANGEStrategy(kvStore)),
                entry(LLEN, new LLENStrategy(kvStore)),
                entry(LPOP, new LPOPStrategy(kvStore)),
                entry(TYPE, new TYPEStrategy(kvStore)),
                entry(XRANGE, new XRANGEStrategy(kvStore))
        ));

        strategies.put(BLPOP, new BLPOPStrategy(kvStore, this));
        strategies.put(RPUSH, new RPUSHStrategy(kvStore, this));
        strategies.put(LPUSH, new LPUSHStrategy(kvStore, this));
        strategies.put(XREAD, new XREADStrategy(kvStore, this));
        strategies.put(XADD, new XADDStrategy(kvStore, this));
        this.asyncCommandObserver = observer;
    }

    public void execute(List<String> args, SocketChannel clientSocket) {
        var command = args.getFirst().toUpperCase();

        var strategy = strategies.getOrDefault(fromString(command), null);
        switch (strategy) {
            case null -> throw new IllegalArgumentException(String.format("Command %s does not exist", command));
            case CommandStrategy syncCommand -> {
                var response = syncCommand.execute(args.subList(1, args.size()));
                asyncCommandObserver.onResponseReady(clientSocket, response);
            }
            case AsyncCommandStrategy asyncCommand ->
                    asyncCommand.executeAsync(args.subList(1, args.size()), clientSocket);
            default -> {
            }
        }

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
        if (this.waitingClients.get(key) != null) {
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
                        // TODO what if other command want to return something else?
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
}