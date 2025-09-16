package commands.strategies.misc;

import commands.CommandStrategy;
import commands.ProtocolUtils;

import java.nio.ByteBuffer;
import java.util.List;

import static commands.Errors.checkArgNumber;

public class ECHOStrategy implements CommandStrategy {
    @Override
    public ByteBuffer execute(List<String> args) {

        var err = checkArgNumber(args, 1, 1);
        if (err != null) {
            return err;
        }

        return ByteBuffer.wrap(
                ProtocolUtils.encode(args.getFirst()).getBytes()
        );
    }
}
