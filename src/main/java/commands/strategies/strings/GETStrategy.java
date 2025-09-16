package commands.strategies.strings;

import commands.CommandStrategy;
import lombok.AllArgsConstructor;
import store.KeyValueStore;
import store.types.DataType;

import java.nio.ByteBuffer;
import java.util.List;

import static commands.Errors.WRONG_TYPE;
import static commands.Errors.checkArgNumber;
import static commands.ProtocolUtils.*;

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
        var valueObject = kvStore.getRedisObject(key);

        if (valueObject == null) {
            return ByteBuffer.wrap(NULL_STRING.getBytes());
        }

        var value = valueObject.getValue();
        var type = valueObject.getType();

        if (!type.equals(DataType.STRING)){
            return ByteBuffer.wrap(encodeSimpleError(WRONG_TYPE).getBytes());
        }

        return ByteBuffer.wrap(
                encode(String.valueOf(value)).getBytes()
        );
    }
}
