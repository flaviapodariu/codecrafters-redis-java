package server;

import parser.Parser;
import store.KeyValueStore;

import java.io.IOException;

import static server.Configuration.TCP_PORT;

public class RedisServer {

    private final Configuration configuration;

    public RedisServer(Configuration configuration) {
        this.configuration = configuration;
    }

    public void run() throws IOException {
        KeyValueStore kvStore = new KeyValueStore();
        Parser parser = new Parser();

        EventLoop eventLoop = new EventLoop(parser, kvStore, configuration);

        var port = configuration.getServer().get(TCP_PORT);
        eventLoop.configure("0.0.0.0", Integer.parseInt(port));
        eventLoop.run();
    }

}
