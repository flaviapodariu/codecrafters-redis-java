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
public class LrangeStrategy implements CommandStrategy {
    private final KeyValueStore kvStore;

    @Override
    public ByteBuffer execute(List<String> args) {
        var err = checkArgNumber(args, 3, 3);
        if (err != null) {
            return err;
        }

        var key = args.getFirst();
        var start = Integer.parseInt(args.get(1));
        var stop = Integer.parseInt(args.get(2));

        var retrievedRange = kvStore.getRange(key, start, stop);

        return ByteBuffer.wrap(
                ProtocolUtils.encode(retrievedRange).getBytes()
        );
    }
}
