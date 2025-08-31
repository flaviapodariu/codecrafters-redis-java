package commands.strategies;

import commands.CommandStrategy;
import commands.ProtocolUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import store.KeyValueStore;

import java.nio.ByteBuffer;
import java.util.List;

import static commands.Errors.checkArgNumber;

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

        var list = (List<String>) kvStore.getValue(key);
        var length = list != null ? list.size() : 0;

        return ByteBuffer.wrap(
                ProtocolUtils.encode(length).getBytes()
        );
    }
}
