package commands;

public class ProtocolUtils {
    public static String TERMINATOR = "\r\n";
    public static String BULK_STRING = "$";
    public static String INTEGER = ":";
    public static String NULL_STRING = "$-1\r\n";

    public static String SIMPLE_ERROR = "-";
    public static String BULK_ERROR = "!";

    public static String OK = "+OK\r\n";


    /**
     * Generic encoder for supported data types
     * @param arg the argument to be encoded
     * @return the RESP encoded argument
     */
    public static String encode(Object arg) {
        if (arg instanceof String stringArg) {
            return bulkEncode(stringArg, BULK_STRING);
        }

        if (arg instanceof Integer number) {
            return simpleEncode(number, INTEGER);
        }

        return "";
    }

    public static String encodeSimpleError(String message) {
        return simpleEncode(message, SIMPLE_ERROR);
    }

    public static String encodeBulkError(String message) {
        return bulkEncode(message, BULK_ERROR);
    }

    private static String simpleEncode(Object arg, String prefix) {
        return prefix + arg + TERMINATOR;
    }

    private static String bulkEncode(String arg, String prefix) {
        return prefix +
                arg.length() +
                TERMINATOR +
                arg +
                TERMINATOR;
    }

}
