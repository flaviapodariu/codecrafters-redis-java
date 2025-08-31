package commands.strategies;

import commands.CommandStrategy;
import lombok.AllArgsConstructor;
import store.KeyValueStore;

import java.nio.ByteBuffer;
import java.util.List;

import static commands.Errors.checkArgNumber;
import static commands.ProtocolUtils.NULL_STRING;
import static commands.ProtocolUtils.encode;

@AllArgsConstructor
public class GETStrategy implements CommandStrategy {

    private final KeyValueStore kvStore;

    @Override
    public ByteBuffer execute(List<String> args) {

        var err = checkArgNumber(args, 1, 1);
        if (err != null) {
            return err;
        }

        var key = args.getFirst();
        var value = kvStore.getValue(key);

        if (value == null) {
            return ByteBuffer.wrap(NULL_STRING.getBytes());
        }

        return ByteBuffer.wrap(
                encode(String.valueOf(value)).getBytes()
        );
    }
}
