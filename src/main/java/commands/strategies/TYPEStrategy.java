package commands.strategies;

import commands.CommandStrategy;
import lombok.AllArgsConstructor;
import store.KeyValueStore;

import java.nio.ByteBuffer;
import java.util.List;

import static commands.Errors.checkArgNumber;
import static commands.ProtocolUtils.encode;

@AllArgsConstructor
public class TYPEStrategy implements CommandStrategy {

    private final KeyValueStore kvStore;

    @Override
    public ByteBuffer execute(List<String> args) {
        var err = checkArgNumber(args, 1, 1);
        if (err != null) {
            return err;
        }

        var key = args.getFirst();

        var valueObject = this.kvStore.getRedisObject(key);

        if (valueObject == null) {
            return ByteBuffer.wrap(
                    encode("none").getBytes()
            );
        }

        var type = valueObject.getType();
        return ByteBuffer.wrap(
                encode(type.toString()).getBytes()
        );
    }
}
