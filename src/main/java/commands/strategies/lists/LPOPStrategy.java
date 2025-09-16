package commands.strategies.lists;

import commands.CommandStrategy;
import commands.ProtocolUtils;
import commands.exceptions.CommandExecutionException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import store.KeyValueStore;

import java.nio.ByteBuffer;
import java.util.List;

import static commands.Errors.checkArgNumber;
import static commands.ProtocolUtils.NULL_STRING;

@Slf4j
@AllArgsConstructor
public class LPOPStrategy implements CommandStrategy {
    private final KeyValueStore kvStore;

    @Override
    public ByteBuffer execute(List<String> args) {
        var err = checkArgNumber(args, 1, 2);
        if (err != null) {
            return err;
        }

        var key = args.getFirst();
        int numItems = 1;

        if (args.size() == 2) {
            numItems = Integer.parseInt(args.get(1));
        }

        try {
            var removedItems = kvStore.removeItems(key, numItems);
            if (removedItems == null) {
                return ByteBuffer.wrap(NULL_STRING.getBytes());
            }

            if (removedItems.size() == 1) {
                return ByteBuffer.wrap(
                        ProtocolUtils.encode(removedItems.getFirst()).getBytes()
                );
            }

            return ByteBuffer.wrap(
                    ProtocolUtils.encode(removedItems).getBytes()
            );
        }  catch (CommandExecutionException ex) {
            log.error(ex.getMessage());
            return ByteBuffer.wrap(
                    ProtocolUtils.encodeSimpleError(ex.getMessage()).getBytes()
            );
        } catch (Exception e) {
            var msg = String.format("Could not remove from list at key %s", key);
            log.error(msg);

            return ByteBuffer.wrap(
                    ProtocolUtils.encodeSimpleError(msg).getBytes()
            );
        }


    }

    public String execute(String key) {
        log.debug("internal lpop execute");
        return this.kvStore.removeFirst(key);
    }
}
