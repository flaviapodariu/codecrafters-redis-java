package commands;

import java.nio.ByteBuffer;
import java.util.List;

public interface CommandStrategy {
    ByteBuffer execute(List<String> args);
}
