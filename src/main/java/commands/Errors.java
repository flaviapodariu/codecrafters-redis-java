package commands;

import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.List;

@Slf4j
public class Errors {
    public static final String WRONG_TYPE = "WRONGTYPE Operation against a key holding the wrong kind of value";
    public static final String STREAM_ID_LOWER = "ERR The ID specified in XADD is equal or smaller than the target stream top item";
    public static final String STREAM_ID_NOT_ALLOWED = "ERR The ID specified in XADD must be greater than 0-0";
    public static final String INVALID_STREAM_ID = "Invalid stream ID specified as stream command argument";
    public static final String COMMAND_FAIL = "Command could not be executed.";
    public static final String UNBALANCED_XREAD = "ERR Unbalanced 'xread' list of streams: for each stream key an ID, '+', or '$' must be specified.";
    public static final String TIMEOUT_INVALID = "ERR timeout is not an integer or out of range";
    public static final String SYNTAX_ERROR = "ERR syntax error";
    public static final String INVALID_ARGS_NUMBER = "ERR wrong number of arguments for command";
    public static final String NOT_AN_INTEGER = "ERR value is not an integer or out of range";
    public static final String NESTED_TRANSACTIONS_ERROR = "ERR MULTI calls can not be nested";
    public static final String EXEC_WITHOUT_TRANSACTION = "ERR EXEC without MULTI";
    public static final String DISCARD_WITHOUT_TRANSACTION = "ERR DISCARD without MULTI";

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
