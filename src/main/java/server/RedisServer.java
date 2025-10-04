package server;

import parser.Parser;
import server.info.ClientsInfo;
import server.info.ReplicationInfo;
import server.info.ServerInfo;
import store.KeyValueStore;

import java.io.IOException;

public class RedisServer {

    private final Configuration configuration;

    public RedisServer(Configuration configuration) {
        this.configuration = configuration;
    }

    public void run() throws IOException {
        KeyValueStore kvStore = new KeyValueStore();
        Parser parser = new Parser();

        EventLoop eventLoop = new EventLoop(parser, kvStore, configuration);

        var port = configuration.getServerInfo().getTcpPort();
        eventLoop.configure("0.0.0.0", port);
        eventLoop.run();
    }

}
