package commands.strategies;

import commands.CommandStrategy;
import commands.ProtocolUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import store.KeyValueStore;

import java.nio.ByteBuffer;
import java.util.List;

import static commands.Errors.checkArgNumber;
import static commands.ProtocolUtils.NULL_STRING;

@Slf4j
@AllArgsConstructor
public class LpopStrategy implements CommandStrategy {
    private final KeyValueStore kvStore;

    @Override
    public ByteBuffer execute(List<String> args) {
        var err = checkArgNumber(args, 1, 1);
        if (err != null) {
            return err;
        }

        var key = args.getFirst();
        var removedItem = kvStore.removeFirst(key);
        if (removedItem == null) {
            return ByteBuffer.wrap(NULL_STRING.getBytes());
        }

        return ByteBuffer.wrap(
                ProtocolUtils.encode(removedItem).getBytes()
        );
    }
}
