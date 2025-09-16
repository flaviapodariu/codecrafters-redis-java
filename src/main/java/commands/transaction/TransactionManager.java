package commands.transaction;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

public interface TransactionManager {

    ByteBuffer onExecuteTransaction(List<List<String>> commandList, SocketChannel channel);

}
