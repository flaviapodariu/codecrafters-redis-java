package commands;

public class ProtocolUtils {
    public static String TERMINATOR = "\r\n";
    public static String BULK_STRING = "$";

    public static String NULL_STRING = "$-1\r\n";

    public static String OK = "+OK\r\n";


    public static String encode(String arg) {
        return BULK_STRING +
                arg.length() +
                TERMINATOR +
                arg +
                TERMINATOR;
    }
}
