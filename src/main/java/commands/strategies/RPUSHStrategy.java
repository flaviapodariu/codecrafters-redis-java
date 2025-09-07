package commands.strategies;

import commands.Command;
import commands.async.BlockingClientManager;
import commands.CommandStrategy;
import commands.ProtocolUtils;
import commands.async.UnblockingMethod;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import store.KeyValueStore;

import java.nio.ByteBuffer;
import java.util.List;

import static commands.Errors.checkArgNumber;

@Slf4j
@AllArgsConstructor
public class RPUSHStrategy implements CommandStrategy {

    private final KeyValueStore kvStore;
    private final BlockingClientManager blockingClientManager;

    @Override
    public ByteBuffer execute(List<String> args) {

        var err = checkArgNumber(args, 2);
        if (err != null) {
            return err;
        }

        var key = args.getFirst();
        List<String> values = args.subList(1, args.size());

        try {
            var elements = kvStore.append(key, values);
            this.blockingClientManager.unblockClient(key, Command.LPOP, UnblockingMethod.FIFO);
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
