package commands;

import commands.strategies.*;
import lombok.Getter;
import lombok.Setter;
import store.KeyValueStore;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.time.Instant;
import java.util.*;

@Getter
@Setter
public class CommandHandler implements BlockingClientManager {

    // todo strategies should be final. fix the blop issue when constructing the map
    private Map<String, Object> strategies;
    private final AsyncCommandObserver asyncCommandObserver;

    private final Map<String, List<SocketChannel>> waitingClients = new HashMap<>();
    private final Map<SocketChannel, String> reverseLookupClient = new HashMap<>();
    private final Map<SocketChannel, Instant> clientTimeouts = new HashMap<>();

    public CommandHandler(KeyValueStore kvStore, AsyncCommandObserver observer) {
        this.strategies = new HashMap<>(Map.of(
                "COMMAND", new DOCSStrategy(),
                "PING", new PINGStrategy(),
                "ECHO", new ECHOStrategy(),
                "GET", new GETStrategy(kvStore),
                "SET", new SETStrategy(kvStore),
                "LRANGE", new LRANGEStrategy(kvStore),
                "LLEN", new LLENStrategy(kvStore),
                "LPOP", new LPOPStrategy(kvStore)
        ));

        strategies.put("BLPOP", new BLPOPStrategy(kvStore, this));
        strategies.put("RPUSH", new RPUSHStrategy(kvStore, this));
        strategies.put("LPUSH", new LPUSHStrategy(kvStore, this));
        this.asyncCommandObserver = observer;

    }

    public void execute(List<String> args, SocketChannel clientSocket) {
        var command = args.getFirst().toUpperCase();

        var strategy = strategies.getOrDefault(command, null);
        if (strategy == null) {
            throw new IllegalArgumentException(String.format("Command %s does not exist", command));
        }

        ByteBuffer response = null;

        if (strategy instanceof CommandStrategy syncCommand) {
            response = syncCommand.execute(args.subList(1, args.size()));
            asyncCommandObserver.onResponseReady(clientSocket, response);
        } else if (strategy instanceof AsyncCommandStrategy asyncCommand) {
            asyncCommand.executeAsync(args.subList(1, args.size()), clientSocket);
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
                // should never be null since we are single threaded
                var removedItem = pop.execute(key);

                var response = Arrays.asList(key, removedItem);
                sendResponse(
                        unblockedClient,
                        ByteBuffer.wrap(
                                ProtocolUtils.encode(response).getBytes())
                );
            }
        }

    }

    @Override
    public void sendResponse(SocketChannel channel, ByteBuffer response) {
        asyncCommandObserver.onResponseReady(channel, response);
    }
}