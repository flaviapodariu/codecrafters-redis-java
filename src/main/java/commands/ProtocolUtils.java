package commands;

public class ProtocolUtils {
    public static String TERMINATOR = "\r\n";
    public static String BULK_STRING = "$";
    public static String INTEGER = ":";
    public static String NULL_STRING = "$-1\r\n";

    public static String OK = "+OK\r\n";


    /**
     * Generic encoder for supported data types
     * @param arg the argument to be encoded
     * @return the RESP encoded argument
     */
    public static String encode(Object arg) {
        if (arg instanceof String stringArg) {
            return BULK_STRING +
                    stringArg.length() +
                    TERMINATOR +
                    arg +
                    TERMINATOR;
        }

        if (arg instanceof Integer number) {
            return INTEGER + number + TERMINATOR;
        }

        return "";
    }

}
