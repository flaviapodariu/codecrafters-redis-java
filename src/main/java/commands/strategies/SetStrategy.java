package commands.strategies;

import commands.CommandStrategy;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import store.*;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.List;

import static commands.ProtocolUtils.NULL_STRING;
import static commands.ProtocolUtils.OK;

@Slf4j
@AllArgsConstructor
public class SetStrategy implements CommandStrategy {

    private final KeyValueStore kvStore;

    /**
     * SET operation implementation.
     * Possible arguments and their indexes:
     * 0 : SET (current op)
     * 1 : key
     * 2 : value
     * 3 : optional argument ( PX / PXAT / EX / EXAT)
     * 4 : a positive integer if an expiry option was provided at 3
     * 5 : optional argument NX / XX (conditional set)
     *
     * @param args the list of provided arguments as explained above
     * @return a protocol encoded "OK" or an error
     */
    @Override
    public ByteBuffer execute(List<String> args) {
        if (args.size() < 3 || args.size() > 6) {
            log.error("Invalid number of arguments for SET operation");
            // TODO
            return ByteBuffer.wrap("".getBytes());
        }

        var key = args.get(1);
        var value = args.get(2);
        var ttl = Long.MAX_VALUE;
        Expiry expiry = null;
        String condition = null;

        int curr = 3;
        while (curr < args.size()) {
            var currArg = args.get(curr).toUpperCase();
            switch (currArg) {
                case "EX":
                case "PX":
                    if (curr + 1 >= args.size()) {
                        //TODO return an error response  NULL for now ...
                        log.error("Optional argument {} requires a positive integer for expiry", args.get(curr));
                        return ByteBuffer.wrap(NULL_STRING.getBytes());
                    }
                    if (currArg.equals("EX")) {
                        ttl = Long.parseLong(args.get(curr+1)) * 1000;
                    } else {
                        ttl = Long.parseLong(args.get(curr+1));
                    }
                    expiry = new TTLExpiry(ttl);
                    curr += 2;
                    break;
                case "EXAT":
                case "PXAT":
                    if (curr + 1 >= args.size()) {
                        //TODO return an error response  NULL for now ...
                        log.error("Optional argument {} requires a positive integer for the expiry timestamp", args.get(curr));
                        return ByteBuffer.wrap(NULL_STRING.getBytes());
                    }

                    if (currArg.equals("EXAT")) {
                        ttl = Long.parseLong(args.get(curr+1)) * 1000;
                    } else {
                        ttl = Long.parseLong(args.get(curr+1));
                    }
                    expiry = new UNIXExpiry(Instant.ofEpochMilli(ttl));
                    curr += 2;
                    break;
                case "NX":
                    condition = "NX";
                    curr += 1;
                    break;
                case "XX":
                    condition = "XX";
                    curr += 1;
                    break;
            }
        }

        if (condition != null && !shouldApplyCondition(condition, key)) {
            log.info("Value was not set. Argument {} was used", condition);
            return ByteBuffer.wrap(NULL_STRING.getBytes());
        }

        expiry = expiry != null ? expiry : new NoExpiry();

        try {
            kvStore.addValue(key, value, expiry);
        } catch (Exception e) {
            log.error("Could not save the key-value pair {}-{}", key, value);
            // TODO
            return ByteBuffer.wrap("".getBytes());
        }

        return ByteBuffer.wrap(OK.getBytes());
    }

    private boolean shouldApplyCondition(String condition, String key) {
        return switch (condition) {
            case "NX" -> !kvStore.containsKey(key);
            case "XX" -> kvStore.containsKey(key);
            default -> false;
        };
    }
}
