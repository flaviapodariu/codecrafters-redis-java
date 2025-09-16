package commands.transaction;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

public interface TransactionalCommandStrategy {

    ByteBuffer execute(List<String> args, SocketChannel channel);
}
