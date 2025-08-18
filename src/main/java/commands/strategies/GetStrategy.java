package commands.strategies;

import commands.CommandStrategy;
import lombok.AllArgsConstructor;
import store.KeyValueStore;

import java.nio.ByteBuffer;
import java.util.List;

import static commands.ProtocolUtils.NULL_STRING;
import static commands.ProtocolUtils.encode;

@AllArgsConstructor
public class GetStrategy implements CommandStrategy {

    private final KeyValueStore kvStore;

    @Override
    public ByteBuffer execute(List<String> args) {
        var value = kvStore.getValue(args.get(1));

        if (value == null) {
            return ByteBuffer.wrap(NULL_STRING.getBytes());
        }

        return ByteBuffer.wrap(
                encode(value).getBytes()
        );
    }
}
