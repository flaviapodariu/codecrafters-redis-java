package commands;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public interface BlockingClientManager {

    void registerBlockingClient(String key, SocketChannel channel, long timeout);

    void unblockClient(String key);

    void sendResponse(SocketChannel channel, ByteBuffer response);
}
