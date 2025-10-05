package server;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class Configuration {
    private Map<String, String> clients;
    private Map<String, String> replication;
    private Map<String, String> server;
    private Map<String, String> memory;


    public static final String TCP_PORT = "tcp_port";
    public static final String CONNECTED_CLIENTS = "connected_clients";
    public static final String CONNECTED_SLAVES = "connected_slaves";
    public static final String ROLE = "role";
    public static final String MASTER_HOST = "master_host";
    public static final String MASTER_PORT = "master_port";

    public Configuration() {
        initClientsSection();
        initReplicationSection();
        initServerSection();
    }

    public Map<String, Map<String, String>> getFullConfig() {
        var fullConfig = new HashMap<String, Map<String, String>>();
        fullConfig.put("clients", this.clients);
        fullConfig.put("replication", this.replication);
        fullConfig.put("server", this.server);
        fullConfig.put("memory", this.memory);
        return fullConfig;
    }

    private void initClientsSection() {
        this.clients = new HashMap<>();
        this.clients.put(CONNECTED_CLIENTS, "0");
    }

    private void initReplicationSection() {
        this.replication = new HashMap<>();
        this.replication.put(ROLE, "master");
        this.replication.put(CONNECTED_SLAVES, "0");
        this.replication.put(MASTER_HOST, "localhost");
        this.replication.put(MASTER_PORT, "6379");
    }

    private void initServerSection() {
        this.server = new HashMap<>();
        this.server.put(TCP_PORT, "6379");
    }

    private void initMemorySection() {
        this.memory = new HashMap<>();
        this.memory.put(CONNECTED_CLIENTS, "0");
    }
}
