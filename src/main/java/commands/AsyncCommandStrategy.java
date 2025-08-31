package commands;

import java.nio.channels.SocketChannel;
import java.util.List;

public interface AsyncCommandStrategy {

    void executeAsync(List<String> args, SocketChannel clientChannel);
}
