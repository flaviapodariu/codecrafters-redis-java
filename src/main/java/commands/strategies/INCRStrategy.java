package commands.strategies;

import commands.CommandStrategy;
import commands.ProtocolUtils;
import commands.exceptions.CommandExecutionException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import store.KeyValueStore;

import java.nio.ByteBuffer;
import java.util.List;

import static commands.Errors.checkArgNumber;

@AllArgsConstructor
@Slf4j
public class INCRStrategy implements CommandStrategy {

    private final KeyValueStore kvStore;

    @Override
    public ByteBuffer execute(List<String> args) {
        var err = checkArgNumber(args, 1, 1);
        if (err != null) {
            return err;
        }

        var key = args.getFirst();

        try {
            var updatedValue = this.kvStore.increment(key);
            return ByteBuffer.wrap(
                    ProtocolUtils.encode(updatedValue).getBytes()
            );
        } catch(CommandExecutionException ex) {
            log.error(ex.getMessage());
            return ByteBuffer.wrap(
                    ProtocolUtils.encodeSimpleError(ex.getMessage()).getBytes()
            );
        } catch (Exception e) {
            var msg = String.format("Could not increment %s...", key);
            log.error(msg);

            return ByteBuffer.wrap(
                    ProtocolUtils.encodeSimpleError(msg).getBytes()
            );
        }
    }
}
