package commands;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.stream.Stream;

public class ProtocolUtils {
    public static String TERMINATOR = "\r\n";

    public static String BULK_STRING = "$";
    public static String INTEGER = ":";
    public static String LIST = "*";

    public static String NULL_STRING = "$-1\r\n";
    public static String NULL_LIST = "*-1\r\n";

    public static String SIMPLE_ERROR = "-";
    // TODO might be wrong
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


    /**
     * Method used for encoding streams.
     * The response is a list a streams, where each stream is represented by a list.
     * This list contains:
     *      1. stream id as string type
     *      2. a list containing the stream's values ( ["key", "value"] )
     * @param stream the stream to encode
     * @return a string holding the encoded response
     */
    public static String encodeStream(SortedMap<String, Map<String, String>> stream) {
        if (stream.isEmpty()) {
            return NULL_LIST;
        }

        var sb = new StringBuilder();
        sb.append(LIST).append(stream.size()).append(TERMINATOR);
        stream.forEach( (id, entry) -> {
            // always an id and a stream entry => 2 items
            sb.append(LIST).append(2).append(TERMINATOR);

            sb.append(bulkEncode(id, BULK_STRING));

            var entryList = entry.entrySet().stream()
                    .flatMap(e -> Stream.of(e.getKey(), e.getValue()))
                    .toList();
            sb.append(encode(entryList));
        });

        return sb.toString();
    }

    /**
     * Method used for encoding expected response for the XREAD operation.
     * The response is a list containing lists. These lists contain:
     *      1. redis object key as string type
     *      2. a list of streams
     *
     * @param streamCollection a structure mapping each redis object key to its streams
     * @return a string holding the encoded response
     */
    public static String encodeStreamList(Map<String, SortedMap<String, Map<String, String>>> streamCollection) {
        if (streamCollection.isEmpty()) {
            return NULL_LIST;
        }

        var sb = new StringBuilder();
        sb.append(LIST).append(streamCollection.size()).append(TERMINATOR);
        streamCollection.forEach( (key, stream) -> {
            sb.append(LIST).append(2).append(TERMINATOR);
            sb.append(bulkEncode(key, BULK_STRING));

            sb.append(encodeStream(stream));
        });

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












