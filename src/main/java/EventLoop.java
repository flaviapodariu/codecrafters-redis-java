import commands.Command;
import lombok.extern.slf4j.Slf4j;
import parser.Parser;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Slf4j
class EventLoop {

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;

    private final Command executor;
    private final Parser parser;

    public EventLoop(Parser parser, Command executor) {
        this.parser = parser;
        this.executor = executor;
    }

    /**
     * This method configures the server socket and registers it with the selector.
     * @param port The port to bind the server to.
     *
     */
    public void configure(int port) {
        try {
            this.selector = Selector.open();
            this.serverSocketChannel = ServerSocketChannel.open();
            this.serverSocketChannel.configureBlocking(false);
            this.serverSocketChannel.bind(new InetSocketAddress(port));
            this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            log.info("Server listening on port {} ...", port);
        } catch (IOException e) {
            log.error("Error while configuring the server socket...{}", e.getMessage());
        }
    }

    /**
     * Main event loop for the socket server. Continuously monitors and handles I/O operations.
     *
     * @throws IOException if an I/O error occurs during processing
     */
    public void run() throws IOException {
        while (true) {
            selector.select();

            Set<SelectionKey> selectedKeys = this.selector.selectedKeys();
            Iterator<SelectionKey> keysIterator = selectedKeys.iterator();

            while (keysIterator.hasNext()) {
                SelectionKey key = keysIterator.next();
                keysIterator.remove();

                if (!key.isValid()) continue;

                if (key.isAcceptable()) {
                    handleAccept(key);
                } else if (key.isReadable()) {
                    handleRead(key);
                } else if (key.isWritable()) {
                    handleWrite(key);
                }
            }
        }
    }


    private void handleAccept(SelectionKey key) throws IOException {
        var serverSocket = (ServerSocketChannel) key.channel();
        SocketChannel clientSocket = serverSocket.accept();

        if (clientSocket != null) {
            clientSocket.configureBlocking(false);
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            clientSocket.register(this.selector, SelectionKey.OP_READ, buffer);
            log.info("Accepted new connection from client at {}", clientSocket.getRemoteAddress());
        }

    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel clientSocket = (SocketChannel) key.channel();
        clientSocket.configureBlocking(false);

        ByteBuffer attachedBuffer = (ByteBuffer) key.attachment();
        int readBytes = clientSocket.read(attachedBuffer);

        if (readBytes == -1) {
            log.info("Client disconnected: {}", clientSocket.getRemoteAddress());
            clientSocket.close();
            key.cancel();
            return;
        }

        if (readBytes > 0) {
            attachedBuffer.flip();

            ByteBuffer output = null;
            var parsedCommand = parser.parse(attachedBuffer);

            if (parsedCommand instanceof List<?> commandItems) {
                if(commandItems.getFirst() instanceof String) {
                    output = executor.execute((List<String>) commandItems);
                }
            }

            if (output != null && output.hasRemaining()) {
                key.attach(output);
                key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            } else {
                key.interestOps(SelectionKey.OP_READ);
            }
            attachedBuffer.compact();
        }
    }

    private void handleWrite(SelectionKey key) throws IOException {
        SocketChannel clientSocket = (SocketChannel) key.channel();
        ByteBuffer writeBuffer = (ByteBuffer) key.attachment();

        if (writeBuffer == null || !writeBuffer.hasRemaining()) {
            log.warn("Nothing to write...");
            key.interestOps(SelectionKey.OP_READ);
            key.attach(ByteBuffer.allocate(1024));
            return;
        }
        int bytesWritten = clientSocket.write(writeBuffer);
        log.debug("Wrote {} bytes to {} (remaining: {})", bytesWritten, clientSocket.getRemoteAddress(), writeBuffer.remaining());

        if (!writeBuffer.hasRemaining()) {
            log.info("Finished sending response to {}", clientSocket.getRemoteAddress());
            key.interestOps(SelectionKey.OP_READ);
            key.attach(ByteBuffer.allocate(1024));
        }
    }

}
