package server;

import parser.Parser;
import store.KeyValueStore;

import java.io.IOException;

public class RedisServer {

    private final String host;
    private final int port;

    public RedisServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() throws IOException {
        KeyValueStore kvStore = new KeyValueStore();
        Parser parser = new Parser();

        EventLoop eventLoop = new EventLoop(parser, kvStore);

        eventLoop.configure(this.host, this.port);
        eventLoop.run();
    }

}
