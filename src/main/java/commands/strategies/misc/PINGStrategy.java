package commands.strategies.misc;

import commands.CommandStrategy;

import java.nio.ByteBuffer;
import java.util.List;

public class PINGStrategy implements CommandStrategy {
    @Override
    public ByteBuffer execute(List<String> args) {
        return ByteBuffer.wrap("+PONG\r\n".getBytes());
    }
}
