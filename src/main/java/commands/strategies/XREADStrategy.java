package commands.strategies;

import commands.Command;
import commands.async.AsyncCommandStrategy;
import commands.async.BlockedClient;
import commands.async.BlockingClientManager;
import commands.ProtocolUtils;
import commands.async.UnblockingMethod;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import store.KeyValueStore;
import store.StreamIdUtils;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static commands.Errors.*;
import static store.StreamIdUtils.getNextId;

@Slf4j
@AllArgsConstructor
public class XREADStrategy implements AsyncCommandStrategy {

    private final KeyValueStore kvStore;
    private final BlockingClientManager blockingClientManager;

    @Override
    public void executeAsync(List<String> args, SocketChannel client) {
        var err = checkArgNumber(args, 3);
        if (err != null) {
            blockingClientManager.sendResponse(client, err);
        }

        var i = 0;
        var shouldBlock = false;
        var timeout = 0L;
        var streamsOptionSeen = false;

        var utils = new StreamIdUtils(kvStore);
        var keys = new ArrayList<String>();
        var ids = new ArrayList<String>();
        var firstIdSeen = false;
        var count = 1;

        while(i < args.size()) {
            var currArg = args.get(i);

            if (currArg.equalsIgnoreCase("STREAMS")) {
                streamsOptionSeen = true;
                i++;
                continue;
            }

            if (streamsOptionSeen) {
                var formatError = utils.checkSimpleId(currArg);

                if (formatError != null) {
                    if (currArg.equals("$")) {
                        ids.add(currArg);
                        i++;

                        firstIdSeen = true;
                        continue;
                    }

                    if (!firstIdSeen) {
                        keys.add(currArg);
                    }
                } else {
                    firstIdSeen = true;
                    ids.add(getNextId(currArg));
                }
            } else {
                if (currArg.equalsIgnoreCase("COUNT")) {
                    // todo
                }

                if (currArg.equalsIgnoreCase("BLOCK")) {
                    shouldBlock = true;
                    try {
                        timeout = Long.parseLong(args.get(i+1));
                    } catch (Exception e) {
                        blockingClientManager.sendResponse(
                                client,
                                ByteBuffer.wrap(
                                        ProtocolUtils.encodeSimpleError(TIMEOUT_INVALID).getBytes()
                                )
                        );
                    }

                    i++;
                }
            }
            i++;
        }

        if (!streamsOptionSeen) {
            blockingClientManager.sendResponse(
                    client,
                    ByteBuffer.wrap(
                            ProtocolUtils.encodeBulkError("stream option").getBytes()
                    ));
        }

        if (keys.size() != ids.size()) {
            blockingClientManager.sendResponse(
                    client,
                    ByteBuffer.wrap(
                            ProtocolUtils.encodeBulkError(UNBALANCED_XREAD).getBytes()
                    ));
        }

        try {
            var streams = this.kvStore.selectStreams(keys, ids, count);

            if (shouldBlock && streams.isEmpty()) {
                var blockedClient = BlockedClient.builder()
                        .channel(client)
                        .executedCommand(Command.XREAD)
                        .method(UnblockingMethod.ALL)
                        .keys(keys)
                        .ids(ids);

                if (timeout > 0) {
                    var instantTimeout = Instant.now().plusMillis(timeout);
                    blockedClient.timeout(instantTimeout);
                }

                keys.forEach(k ->
                        blockingClientManager.registerBlockingClient(
                                k, blockedClient.build())
                );
                return;
            }

            blockingClientManager.sendResponse(
                    client,
                    ByteBuffer.wrap(
                            ProtocolUtils.encodeStreamList(streams, keys).getBytes()
                    ));
        } catch (Exception e) {
            log.error(COMMAND_FAIL);
            blockingClientManager.sendResponse(
                    client,
                    ByteBuffer.wrap(
                            ProtocolUtils.encodeBulkError(COMMAND_FAIL).getBytes()
                    ));
        }

    }

    public ByteBuffer execute(List<String> keys, List<String> ids, int count) {
        try {
            var selectedStreams = this.kvStore.selectStreams(keys, ids, count);
            return ByteBuffer.wrap(
                    ProtocolUtils.encodeStreamList(selectedStreams, keys).getBytes()
            );
        } catch (Exception e) {
            return
                    ByteBuffer.wrap(
                            ProtocolUtils.encodeBulkError(COMMAND_FAIL).getBytes()
                    );
        }

    }
}
