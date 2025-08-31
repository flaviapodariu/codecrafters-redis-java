package commands.strategies;

import commands.CommandStrategy;
import commands.ProtocolUtils;

import java.nio.ByteBuffer;
import java.util.List;

public class DOCSStrategy implements CommandStrategy {

    @Override
    public ByteBuffer execute(List<String> args) {
        return ByteBuffer.wrap(
                ProtocolUtils.encode("DOCS placeholder").getBytes()
        );
    }
}
