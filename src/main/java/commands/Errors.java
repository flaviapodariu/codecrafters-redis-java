package commands;

import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.List;

@Slf4j
public class Errors {
    private static final String INVALID_ARGS_NUMBER = "Invalid number of arguments for %s operation";


    public static ByteBuffer checkArgNumber(List<String> args, int minArgs) {
        return checkArgNumber(args, minArgs, Integer.MAX_VALUE);
    }

    public static ByteBuffer checkArgNumber(List<String> args, int minArgs, int maxArgs) {
        var command = args.getFirst();
        if (args.size() < minArgs || args.size() > maxArgs) {
            var msg = String.format(INVALID_ARGS_NUMBER, command);
            log.error(msg);
            return ByteBuffer.wrap(
                    ProtocolUtils.encodeSimpleError(msg).getBytes()
            );
        }
        return null;
    }

}
