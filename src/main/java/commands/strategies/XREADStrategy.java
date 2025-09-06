package commands.strategies;

import commands.CommandStrategy;
import commands.ProtocolUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import store.KeyValueStore;
import store.StreamIdUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static commands.Errors.*;

@Slf4j
@AllArgsConstructor
public class XREADStrategy implements CommandStrategy {

    private final KeyValueStore kvStore;
    @Override
    public ByteBuffer execute(List<String> args) {
        var err = checkArgNumber(args, 3);
        if (err != null) {
            return err;
        }

        var streamsOption = args.getFirst();
        var count = 1;
        if (!streamsOption.equals("streams")) {
            return ByteBuffer.wrap(
                    ProtocolUtils.encodeSimpleError("idk what err").getBytes()
            );
        }

        var utils = new StreamIdUtils(kvStore);

        var keys = new ArrayList<String>();
        var ids = new ArrayList<String>();
        var firstIdSeen = false;
        for (int i = 1; i < args.size(); i++) {
            var currArg = args.get(i);
            var formatError = utils.checkSimpleId(currArg);

            if (formatError != null) {
                if (!firstIdSeen) {
                    keys.add(currArg);
                } else {
                    return formatError;
                }
            } else {
                ids.add(utils.getNextId(currArg));
            }
        }

        if (keys.size() != ids.size()) {
            return ByteBuffer.wrap(
                    ProtocolUtils.encodeBulkError(UNBALANCED_XREAD).getBytes()
            );
        }

        try {
            var streams = this.kvStore.selectStreams(keys, ids, count);
            return ByteBuffer.wrap(
                    ProtocolUtils.encodeStreamList(streams).getBytes()
            );
        } catch (Exception e) {
            log.error(COMMAND_FAIL);
            return ByteBuffer.wrap(
                    ProtocolUtils.encodeSimpleError(COMMAND_FAIL).getBytes()
            );
        }

    }
}
