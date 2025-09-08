package commands.async;

import commands.Command;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public interface BlockingClientManager {

    void registerBlockingClient(String key, BlockedClient blockedClient);

    void updateStreamIdForBlockedClient(String key, String lastStreamId);

    void unblockClient(String key, Command waitingFor, UnblockingMethod method);

    void sendResponse(SocketChannel channel, ByteBuffer response);
}
