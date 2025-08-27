package commands.strategies;

import commands.CommandStrategy;
import commands.ProtocolUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import store.KeyValueStore;

import java.nio.ByteBuffer;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class RpushStrategy implements CommandStrategy {

    private final KeyValueStore kvStore;

    @Override
    public ByteBuffer execute(List<String> args) {

        var key = args.get(1);
        var value = args.get(2);
        try {
            var elements = kvStore.append(key, value);
            return ByteBuffer.wrap(
                    ProtocolUtils.encode(elements).getBytes()
            );
        } catch (Exception e) {
            log.error("Could not append to the list at key {}", key);
        }
        return null;
    }
}
