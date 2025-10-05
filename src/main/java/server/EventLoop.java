package server;

import commands.Command;
import commands.async.AsyncCommandObserver;
import commands.CommandHandler;
import commands.async.BlockedClient;
import lombok.extern.slf4j.Slf4j;
import parser.Parser;
import store.KeyValueStore;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static commands.ProtocolUtils.NULL_LIST;

@Slf4j
class EventLoop implements AsyncCommandObserver {

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;

    private final CommandHandler executor;
    private final Parser parser;

    public EventLoop(Parser parser, KeyValueStore kvStore,  Configuration configuration) {
        this.parser = parser;
        this.executor = new CommandHandler(kvStore, this, configuration);
    }

    /**
     * Callback for async commands to register responses for waiting clients
     * @param channel client's socket channel
     * @param response the response
     */
    @Override
    public void onResponseReady(SocketChannel channel, ByteBuffer response) {
        try {
            channel.register(this.selector, SelectionKey.OP_WRITE,  response);
            selector.wakeup();
        } catch (IOException e) {
            log.error("Could not register response");
        }
    }

    /**
     * This method configures the server socket and registers it with the selector.
     * @param port The port to bind the server to.
     *
     */
    public void configure(String host, int port) {
        try {
            this.selector = Selector.open();
            this.serverSocketChannel = ServerSocketChannel.open();
            this.serverSocketChannel.configureBlocking(false);
            this.serverSocketChannel.bind(new InetSocketAddress(host, port));
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
            var t = nextWakeUpMillis();
            selector.select(t);

            checkClientTimeouts();

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

    public void handleRead(SelectionKey key) throws IOException {
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

            var parsedCommand = parser.parse(attachedBuffer);

            if (parsedCommand instanceof List<?> commandItems) {
                if(commandItems.getFirst() instanceof String) {
                    // TODO try to fix ugly cast
                    executor.execute((List<String>) commandItems, clientSocket);
                }
            }
        }
    }

    private void checkClientTimeouts() {
        var waitingClients = this.executor.getWaitingClients();
        if (!waitingClients.isEmpty()) {

            waitingClients.forEach( (waitingForKey, blockedClients) -> {
                blockedClients.forEach( blockedClient -> {
                    var t = blockedClient.getTimeout();
                    var client = blockedClient.getChannel();
                    var method = blockedClient.getMethod();

                    if (t != null && t.isBefore(Instant.now())) {
                        this.executor.unblockClient(waitingForKey, Command.NO_COMMAND, method);
                        onResponseReady(client, ByteBuffer.wrap(NULL_LIST.getBytes()));
                    }
                });
            });

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

    /**
     * When there are no operations left in the event loop's queue
     * and there are clients waiting in the list, they will never be released,
     * even after their timeouts expire, because the server will be idling.
     * This forces the selector to wake up when a client is due to be released.
     * @return a value in milliseconds
     */
    private long nextWakeUpMillis() {
        var minTimeout = this.executor.getWaitingClients().values()
                .stream()
                .flatMap(List::stream)
                .map(BlockedClient::getTimeout)
                .filter(Objects::nonNull)
                .mapToLong(t -> t.toEpochMilli() - Instant.now().toEpochMilli())
                .min()
                .orElse(0L);

        return Math.max(minTimeout, 0L);
    }

}
