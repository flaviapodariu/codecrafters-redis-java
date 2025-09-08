package commands.strategies;

import commands.async.BlockingClientManager;
import commands.CommandStrategy;
import commands.ProtocolUtils;
import commands.async.UnblockingMethod;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import store.KeyValueStore;
import store.StreamIdUtils;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;

import static commands.Command.XREAD;
import static commands.Errors.checkArgNumber;
import static store.StreamIdUtils.checkIllegalStructure;

@Slf4j
@AllArgsConstructor
public class XADDStrategy implements CommandStrategy {

    private final KeyValueStore kvStore;
    private final BlockingClientManager blockingClientManager;

    @Override
    public ByteBuffer execute(List<String> args) {
        var err = checkArgNumber(args, 4);
        if (err != null) {
            return err;
        }

        var streamKey = args.getFirst();
        var streamId = args.get(1);

        var utils = new StreamIdUtils(kvStore);

        if (streamId.equals("*")) {
            streamId = utils.generateFullId();
        }

        var illegalStructure = checkIllegalStructure(streamId);
        if (illegalStructure != null) {
            return illegalStructure;
        }

        streamId = utils.formatId(streamId, streamKey);

        var timestampError = utils.getTimestampErrors(streamKey, streamId);
        if (timestampError != null) {
            return timestampError;
        }

        var item = new HashMap<String, String>();
        for (int i = 2; i < args.size()-1; i += 2) {
            var entryKey = args.get(i);
            var entryValue = args.get(i+1);
            item.put(entryKey, entryValue);
        }

        try {
            kvStore.addStreamValue(streamKey, streamId, item);
            String finalStreamId = streamId;
            item.forEach((_, _) -> {
                        blockingClientManager.updateStreamIdForBlockedClient(streamKey, finalStreamId);
                        blockingClientManager.unblockClient(streamKey, XREAD, UnblockingMethod.ALL);
                    }
            );

            return ByteBuffer.wrap(
                    ProtocolUtils.encode(streamId).getBytes()
            );
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return ByteBuffer.wrap(
                    ProtocolUtils.encodeSimpleError("Could not add stream").getBytes()
            );
        }
    }

}
