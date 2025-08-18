package commands.strategies;

import commands.CommandStrategy;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import store.KeyValueStore;

import java.nio.ByteBuffer;
import java.util.List;

import static commands.ProtocolUtils.OK;

@Slf4j
@AllArgsConstructor
public class SetStrategy implements CommandStrategy {

    private final KeyValueStore kvStore;

    @Override
    public ByteBuffer execute(List<String> args) {
        var key = args.get(1);
        var value = args.get(2);
        try {
            kvStore.addValue(key, value);
        } catch (Exception e) {
            log.error("Could not save the key-value pair {}-{}", key, value);
            return ByteBuffer.wrap("".getBytes());
        }

        return ByteBuffer.wrap(OK.getBytes());
    }
}
