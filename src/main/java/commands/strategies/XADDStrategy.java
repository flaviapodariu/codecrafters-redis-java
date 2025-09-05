package commands.strategies;

import commands.CommandStrategy;
import commands.ProtocolUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import store.KeyValueStore;
import store.StreamIdUtils;
import store.types.StreamObject;

import java.nio.ByteBuffer;
import java.util.List;

import static commands.Errors.*;

@Slf4j
@AllArgsConstructor
public class XADDStrategy implements CommandStrategy {

    private final KeyValueStore kvStore;

    @Override
    public ByteBuffer execute(List<String> args) {
        var err = checkArgNumber(args, 4);
        if (err != null) {
            return err;
        }

        var streamKey = args.getFirst();
        var streamId = args.get(1);

        var utils = new StreamIdUtils(kvStore);
        var illegalStructure = utils.checkIllegalStructure(streamId);
        if (illegalStructure != null) {
            return illegalStructure;
        }

        streamId = utils.formatId(streamId, streamKey);

        var timestampError = utils.getTimestampErrors(streamKey, streamId);
        if (timestampError != null) {
            return timestampError;
        }

        var entryKey = args.get(2);
        var entryValue = args.get(3);

        var streamValue = new StreamObject.StreamValue(streamId, entryKey, entryValue);

        try {
            kvStore.addStreamValue(streamKey, streamValue);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return ByteBuffer.wrap(
                    ProtocolUtils.encodeSimpleError("Could not add stream").getBytes()
            );
        }

        return ByteBuffer.wrap(
                ProtocolUtils.encode(streamId).getBytes()
        );

    }

}
