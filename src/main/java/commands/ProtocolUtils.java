package commands;

import java.util.List;

public class ProtocolUtils {
    public static String TERMINATOR = "\r\n";

    public static String BULK_STRING = "$";
    public static String INTEGER = ":";
    public static String LIST = "*";

    public static String NULL_STRING = "$-1\r\n";
    public static String NULL_LIST = "*0\r\n";

    public static String SIMPLE_ERROR = "-";
    public static String BULK_ERROR = "!";

    public static String OK = "+OK\r\n";


    /**
     * String encoder for supported data types
     * @param arg the argument to be encoded
     * @return the RESP encoded argument
     */
    public static String encode(String arg) {
        return bulkEncode(arg, BULK_STRING);
    }


    /**
     * Integer encoder for supported data types
     * @param number the argument to be encoded
     * @return the RESP encoded argument
     */
    public static String encode(Integer number) {
        return simpleEncode(number, INTEGER);
    }

    /**
     * Integer encoder for supported data types
     * @param list the argument to be encoded
     * @return the RESP encoded argument
     */
    public static String encode(List<String> list) {
        var sb = new StringBuilder();
        sb.append(LIST).append(list.size()).append(TERMINATOR);
        for (var item : list) {
            sb.append(bulkEncode(item, BULK_STRING));
        }
        return sb.toString();
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
