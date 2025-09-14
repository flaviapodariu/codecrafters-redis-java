package commands.strategies;

import commands.CommandStrategy;
import commands.ProtocolUtils;
import commands.exceptions.CommandExecutionException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import store.KeyValueStore;

import java.nio.ByteBuffer;
import java.util.List;

import static commands.Errors.WRONG_TYPE;
import static commands.Errors.checkArgNumber;

@Slf4j
@AllArgsConstructor
public class LRANGEStrategy implements CommandStrategy {
    private final KeyValueStore kvStore;

    @Override
    public ByteBuffer execute(List<String> args) {
        var err = checkArgNumber(args, 3, 3);
        if (err != null) {
            return err;
        }

        var key = args.getFirst();
        var start = Integer.parseInt(args.get(1));
        var stop = Integer.parseInt(args.get(2));

        try {
            var retrievedRange = kvStore.getRange(key, start, stop);

            return ByteBuffer.wrap(
                    ProtocolUtils.encode(retrievedRange).getBytes()
            );
        }  catch (CommandExecutionException ex) {
            log.error(ex.getMessage());
            return ByteBuffer.wrap(
                    ProtocolUtils.encodeSimpleError(ex.getMessage()).getBytes()
            );
        } catch (Exception e) {
            var msg = String.format("Could not retrieve range of list at key %s", key);
            log.error(msg);

            return ByteBuffer.wrap(
                    ProtocolUtils.encodeSimpleError(msg).getBytes()
            );
        }

    }
}
