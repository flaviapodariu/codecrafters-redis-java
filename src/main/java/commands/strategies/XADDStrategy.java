package commands.strategies;

import commands.CommandStrategy;
import commands.ProtocolUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import store.KeyValueStore;
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

        if (streamId.equals("*")) {
            streamId = generateKey();
        } else {
            err = validateId(streamKey, streamId);
            if ( err != null) {
                return err;
            }
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

    private String generateKey() {
        // TODO
        return "";
    }

    private ByteBuffer validateId(String key, String streamId) {
        if (streamId.equals("0-0")) {
            return ByteBuffer.wrap(
                    ProtocolUtils.encodeSimpleError(STREAM_ID_NOT_ALLOWED).getBytes()
            );
        }

        var streamObject = this.kvStore.getRedisObject(key);
        if (streamObject == null) { return null; }

        var stream = (StreamObject) streamObject.getValue();

        var lastEntry = stream.getLast();
        if (lastEntry == null) {
            return null;
        }
        var lastEntrySplitId = lastEntry.getId().split("-");
        var lastIdTimestamp = Long.parseLong(lastEntrySplitId[0]);
        var lastIdSequence = Long.parseLong(lastEntrySplitId[1]);

        var splitId = streamId.split("-");
        var idTimestamp = Long.parseLong(splitId[0]);
        var idSequence = Long.parseLong(splitId[1]);

        if (idTimestamp < lastIdTimestamp || ((idTimestamp == lastIdTimestamp) && (idSequence <= lastIdSequence)) ) {
            return ByteBuffer.wrap(
                    ProtocolUtils.encodeSimpleError(STREAM_ID_LOWER).getBytes()
            );
        }

        return null;
    }
}

//        // TODO kinda buggy, values not existing is not checked
//        for (int i = 2; i < args.size() - 2; i += 3) {
//            var streamId = args.get(i);
//            var key = args.get(i+1);
//            var value = args.get(i+2);
//
//            if (streamId.equals("*")) {
//                streamId = generateKey();
//            } else {
//                err = validateId(streamId);
//                if ( err != null) {
//                    return err;
//                }
//            }
//
//            var streamValue = new StreamObject.StreamValue(streamId, key, value);
//            streamValues.add(streamValue);
//        }
