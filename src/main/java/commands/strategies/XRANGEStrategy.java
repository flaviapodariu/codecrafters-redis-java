package commands.strategies;

import commands.CommandStrategy;
import commands.ProtocolUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import store.KeyValueStore;
import store.StreamIdUtils;

import java.nio.ByteBuffer;
import java.util.List;

import static commands.Errors.COMMAND_FAIL;
import static commands.Errors.checkArgNumber;

@Slf4j
@AllArgsConstructor
public class XRANGEStrategy implements CommandStrategy {

    private final KeyValueStore kvStore;

    @Override
    public ByteBuffer execute(List<String> args) {
        var err = checkArgNumber(args, 3, 3);
        if (err != null) {
            return err;
        }

        var key = args.getFirst();

        var utils = new StreamIdUtils(kvStore);
        var start = args.get(1);
        var end = args.get(2);

        var formatError = utils.checkRangeIllegalStructure(start);
        if (formatError != null) {
            return formatError;
        }

        formatError = utils.checkRangeIllegalStructure(end);
        if (formatError != null) {
            return formatError;
        }
        start = utils.getFormattedStartLimit(start);
        end = utils.getExclusiveEndLimit(end);

        try {
            var streamRange = this.kvStore.getStreamRange(key, start, end);
            return ByteBuffer.wrap(
                    ProtocolUtils.encodeStream(streamRange).getBytes()
            );
        } catch (Exception e) {
            log.error(COMMAND_FAIL);
            return ByteBuffer.wrap(
                    ProtocolUtils.encodeSimpleError(COMMAND_FAIL).getBytes()
            );
        }
    }
}
