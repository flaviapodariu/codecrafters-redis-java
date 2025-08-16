import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

@Slf4j
@NoArgsConstructor
class EventLoop {

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;

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
            String rawCommand = new String(attachedBuffer.array(), 0, readBytes).trim();
            log.debug("Received command {} from client {}", rawCommand, clientSocket.getRemoteAddress());

//            Some light commands like PING will stay inside the read command handler to avoid context switch
            String response = "+PONG\r\n";
            ByteBuffer responseBuffer = ByteBuffer.wrap(response.getBytes());
            while (responseBuffer.hasRemaining()) {
                clientSocket.write(responseBuffer);
            }


        }
    }

    private void handleWrite(SelectionKey key) {

    }

}
