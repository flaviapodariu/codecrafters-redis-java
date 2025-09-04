package commands.strategies;

import commands.CommandStrategy;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import store.types.DataType;
import store.KeyValueStore;

import java.nio.ByteBuffer;
import java.util.List;

import static commands.Errors.WRONG_TYPE;
import static commands.Errors.checkArgNumber;
import static commands.ProtocolUtils.*;

@Slf4j
@AllArgsConstructor
public class LLENStrategy implements CommandStrategy {

    private final KeyValueStore kvStore;

    @Override
    public ByteBuffer execute(List<String> args) {
        var err = checkArgNumber(args, 1, 1);
        if (err != null) {
            return err;
        }

        var key = args.getFirst();

        var valueObject = kvStore.getValueObject(key);

        if (valueObject == null) {
            return ByteBuffer.wrap(encode(0).getBytes());
        }

        List<?> list = valueObject.getValue();
        var type = valueObject.getType();

        if (!type.equals(DataType.LIST)){
            return ByteBuffer.wrap(encodeBulkError(WRONG_TYPE).getBytes());
        }

        var length = list != null ? list.size() : 0;

        return ByteBuffer.wrap(
                encode(length).getBytes()
        );
    }
}
