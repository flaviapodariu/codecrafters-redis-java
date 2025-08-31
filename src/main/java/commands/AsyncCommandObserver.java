package commands;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public interface AsyncCommandObserver {

    void onResponseReady(SocketChannel channel, ByteBuffer response);
}
