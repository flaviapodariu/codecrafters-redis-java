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
public class RpushStrategy implements CommandStrategy {

    private final KeyValueStore kvStore;

    @Override
    public ByteBuffer execute(List<String> args) {

        var err = checkArgNumber(args, 3);
        if (err != null) {
            return err;
        }

        var key = args.get(1);
        List<String> values = args.subList(2, args.size());

        try {
            var elements = kvStore.append(key, values.toArray(new String[0]));
            return ByteBuffer.wrap(
                    ProtocolUtils.encode(elements).getBytes()
            );
        } catch (Exception e) {
            var msg = String.format("Could not append to the list at key %s", key);
            log.error(msg);

            return ByteBuffer.wrap(
                    ProtocolUtils.encodeSimpleError(msg).getBytes()
            );
        }
    }

}
