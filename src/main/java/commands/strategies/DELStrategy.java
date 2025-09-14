package commands.strategies;

import commands.CommandStrategy;
import commands.ProtocolUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import store.KeyValueStore;

import java.nio.ByteBuffer;
import java.util.List;

import static commands.Errors.COMMAND_FAIL;
import static commands.Errors.checkArgNumber;
import static commands.ProtocolUtils.encode;

@Slf4j
@AllArgsConstructor
public class DELStrategy implements CommandStrategy {
    private final KeyValueStore kvStore;

    @Override
    public ByteBuffer execute(List<String> args) {
        var err = checkArgNumber(args, 1);

        if (err != null) {
            return err;
        }

        try {
            var removedItems = this.kvStore.deleteKeys(args);
            return ByteBuffer.wrap(
                    encode(removedItems).getBytes()
            );
        } catch (Exception e) {
            log.error(COMMAND_FAIL);
            return ByteBuffer.wrap(
                    ProtocolUtils.encodeSimpleError(e.getMessage()).getBytes()
            );
        }
    }
}
