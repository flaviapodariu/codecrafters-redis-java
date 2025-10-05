package server;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Getter
@Setter
public class Configuration {
    private Map<String, String> clients;
    private Map<String, String> replication;
    private Map<String, String> server;
    private Map<String, String> memory;

//    CLIENTS
    public static final String CONNECTED_CLIENTS = "connected_clients";

//    REPLICATION
    public static final String CONNECTED_SLAVES = "connected_slaves";
    public static final String ROLE = "role";
    public static final String MASTER_HOST = "master_host";
    public static final String MASTER_PORT = "master_port";
    public static final String MASTER_REPLID = "master_replid";
    public static final String MASTER_REPL_OFFSET = "master_repl_offset";

//    SERVER
    public static final String TCP_PORT = "tcp_port";


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
        this.replication.put(MASTER_REPLID, generateMasterReplid());
        this.replication.put(MASTER_REPL_OFFSET, "0");
    }

    private void initServerSection() {
        this.server = new HashMap<>();
        this.server.put(TCP_PORT, "6379");
    }

    private void initMemorySection() {
        this.memory = new HashMap<>();
        this.memory.put(CONNECTED_CLIENTS, "0");
    }

    private String generateMasterReplid() {
        String characterSet = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder replid = new StringBuilder();
        Random rnd = new Random();
        while (replid.length() < 40) {
            int index = (int) (rnd.nextFloat() * characterSet.length());
            replid.append(characterSet.charAt(index));
        }
        return replid.toString();

    }
}
