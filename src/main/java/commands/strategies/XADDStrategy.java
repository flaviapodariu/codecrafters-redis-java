package commands.strategies;

import commands.CommandStrategy;
import commands.ProtocolUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import store.KeyValueStore;
import store.types.StreamObject;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static commands.Errors.checkArgNumber;

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
        }

        var streamObject = new StreamObject(new ArrayList<>());
        // TODO kinda buggy, values not existing is not checked
        for (int i = 2; i < args.size() - 1; i += 2) {
            var key = args.get(i);
            streamObject.getValue().add(Map.of(streamKey, key));
        }

        try {
            kvStore.addStreamValue(streamKey, streamObject);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return ByteBuffer.wrap(
                    ProtocolUtils.encodeBulkError("Could not add stream").getBytes()
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
}
