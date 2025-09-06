package commands;

import commands.strategies.*;
import lombok.Getter;
import lombok.Setter;
import store.KeyValueStore;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Map.entry;

@Getter
@Setter
public class CommandHandler implements BlockingClientManager {

    // todo strategies should be final. fix the blop issue when constructing the map
    private Map<String, Object> strategies;
    private final AsyncCommandObserver asyncCommandObserver;

    private final Map<String, List<SocketChannel>> waitingClients = new ConcurrentHashMap<>();
    private final Map<SocketChannel, String> reverseLookupClient = new ConcurrentHashMap<>();
    private final Map<SocketChannel, Instant> clientTimeouts = new ConcurrentHashMap<>();

    public CommandHandler(KeyValueStore kvStore, AsyncCommandObserver observer) {
        this.strategies = new HashMap<>(Map.ofEntries(
                entry("COMMAND", new DOCSStrategy()),
                entry("PING", new PINGStrategy()),
                entry("ECHO", new ECHOStrategy()),
                entry("GET", new GETStrategy(kvStore)),
                entry("SET", new SETStrategy(kvStore)),
                entry("LRANGE", new LRANGEStrategy(kvStore)),
                entry("LLEN", new LLENStrategy(kvStore)),
                entry("LPOP", new LPOPStrategy(kvStore)),
                entry("TYPE", new TYPEStrategy(kvStore)),
                entry("XADD", new XADDStrategy(kvStore)),
                entry("XRANGE", new XRANGEStrategy(kvStore)),
                entry("XREAD", new XREADStrategy(kvStore))
        ));

        strategies.put("BLPOP", new BLPOPStrategy(kvStore, this));
        strategies.put("RPUSH", new RPUSHStrategy(kvStore, this));
        strategies.put("LPUSH", new LPUSHStrategy(kvStore, this));
        this.asyncCommandObserver = observer;
    }

    public void execute(List<String> args, SocketChannel clientSocket) {
        var command = args.getFirst().toUpperCase();

        var strategy = strategies.getOrDefault(command, null);
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
     * @param channel the client's socket channel
     * @param timeout the timeout in ms
     */
    @Override
    public void registerBlockingClient(String key, SocketChannel channel, long timeout) {
        this.waitingClients.computeIfAbsent(key, x -> new LinkedList<>());
        this.waitingClients.get(key).add(channel);
        this.reverseLookupClient.put(channel, key);

        if (timeout > 0) {
            var instantTimeout = Instant.now().plusMillis(timeout);
            this.clientTimeouts.put(channel, instantTimeout);
        }
    }

    @Override
    public void unblockClient(String key) {
        if (this.waitingClients.get(key) != null) {
            var unblockedClient = this.waitingClients.get(key).removeFirst();
            if (unblockedClient != null) {
                this.clientTimeouts.remove(unblockedClient);
                this.reverseLookupClient.remove(unblockedClient);

                var pop = (LPOPStrategy) this.strategies.get("LPOP");

                var removedItem = pop.execute(key);

                if (removedItem != null) {
                    var response = Arrays.asList(key, removedItem);
                    sendResponse(
                            unblockedClient,
                            ByteBuffer.wrap(
                                    ProtocolUtils.encode(response).getBytes())
                    );
                }
            }
        }

    }

    @Override
    public void sendResponse(SocketChannel channel, ByteBuffer response) {
        asyncCommandObserver.onResponseReady(channel, response);
    }
}