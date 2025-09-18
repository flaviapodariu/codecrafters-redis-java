package commands.strategies.lists;

import commands.Command;
import commands.CommandStrategy;
import commands.async.AsyncCommandStrategy;
import commands.async.BlockedClient;
import commands.async.BlockingClientManager;
import commands.ProtocolUtils;
import commands.async.UnblockingMethod;
import commands.exceptions.CommandExecutionException;
import commands.transaction.TransactionalCommandStrategy;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import store.KeyValueStore;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static commands.Errors.checkArgNumber;
import static commands.ProtocolUtils.NULL_STRING;

@Slf4j
@AllArgsConstructor
public class BLPOPStrategy implements AsyncCommandStrategy, TransactionalCommandStrategy {

    private final KeyValueStore kvStore;
    private final BlockingClientManager blockingClientManager;

    @Override
    public void executeAsync(List<String> args, SocketChannel client) {

        var err = checkArgNumber(args, 2);
        if (err != null) {
            blockingClientManager.sendResponse(client, err);
        }

        var key = args.getFirst();
        var timeout = (long) (Double.parseDouble(args.get(1)) * 1000);

        try {
            if (kvStore.containsKey(key) && !kvStore.getRange(key, 0, -1).isEmpty()) {
                var removedItem = kvStore.removeFirst(key);
                var response = Arrays.asList(key, removedItem);

                blockingClientManager.sendResponse(
                        client,
                        ByteBuffer.wrap(
                                ProtocolUtils.encode(response).getBytes())
                );
            } else {
                var blockedClient = BlockedClient.builder()
                        .channel(client)
                        .executedCommand(Command.BLPOP)
                        .method(UnblockingMethod.FIFO)
                        .keys(List.of(key));
                if (timeout > 0) {
                    var instantTimeout = Instant.now().plusMillis(timeout);
                    blockedClient.timeout(instantTimeout);
                }

                blockingClientManager.registerBlockingClient(key, blockedClient.build());
            }

        } catch (CommandExecutionException ex) {
            log.error(ex.getMessage());
            blockingClientManager.sendResponse(client, ByteBuffer.wrap(
                    ProtocolUtils.encodeSimpleError(ex.getMessage()).getBytes())
            );
        } catch (Exception ex) {
            var msg = String.format("Could not append to the list at key %s", key);
            log.error(msg);
            blockingClientManager.sendResponse(client, ByteBuffer.wrap(
                    ProtocolUtils.encodeSimpleError(msg).getBytes())
            );
        }
    }

    @Override
    public ByteBuffer execute(List<String> args, SocketChannel channel) {
        // todo
        return ByteBuffer.wrap(NULL_STRING.getBytes());
    }
}
