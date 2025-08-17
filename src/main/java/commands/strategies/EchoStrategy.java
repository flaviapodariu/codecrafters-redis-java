package commands.strategies;

import commands.CommandStrategy;
import commands.ProtocolUtils;

import java.nio.ByteBuffer;
import java.util.List;

public class EchoStrategy implements CommandStrategy {
    @Override
    public ByteBuffer execute(List<String> args) {
        if (args.size() == 2) {
            return ByteBuffer.wrap(
                    ProtocolUtils.encode(args.get(1)).getBytes()
            );
        }
        throw new IllegalArgumentException("Incorrect number of arguments");
    }
}
