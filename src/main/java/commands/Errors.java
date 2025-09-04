package commands;

import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.List;

@Slf4j
public class Errors {
    public static final String WRONG_TYPE = "WRONGTYPE Operation against a key holding the wrong kind of value";
    public static final String STREAM_ID_LOWER = "ERR The ID specified in XADD is equal or smaller than the target stream top item";
    public static final String STREAM_ID_NOT_ALLOWED = "ERR The ID specified in XADD must be greater than 0-0";

    private static final String INVALID_ARGS_NUMBER = "ERR wrong number of arguments for command";


    public static ByteBuffer checkArgNumber(List<String> args, int minArgs) {
        return checkArgNumber(args, minArgs, Integer.MAX_VALUE);
    }

    public static ByteBuffer checkArgNumber(List<String> args, int minArgs, int maxArgs) {
        if (args.size() < minArgs || args.size() > maxArgs) {
            log.error(INVALID_ARGS_NUMBER);
            return ByteBuffer.wrap(
                    ProtocolUtils.encodeSimpleError(INVALID_ARGS_NUMBER).getBytes()
            );
        }
        return null;
    }

}
