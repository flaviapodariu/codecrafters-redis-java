package server;

import commands.ProtocolUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

import static server.Configuration.MASTER_HOST;
import static server.Configuration.TCP_PORT;

@Slf4j
public class ReplicationConnection {

    private final Configuration config;

    public ReplicationConnection(Configuration config) {
        this.config = config;
    }

    public void startReplicaConnection() {
        var replicaHost = config.getReplication().get(MASTER_HOST);
        var replicaPort = config.getServer().get(TCP_PORT);
        try {
            var socket = new Socket(replicaHost, Integer.parseInt(replicaPort));
            pingMaster(socket);
        } catch (IOException e) {
            log.error("Could not connect to the replica at port {}", replicaPort);
        } catch (Exception e) {
            log.error("An exception occurred while connecting to the replica at port {}", replicaPort);
        }
    }

    private void pingMaster(Socket socket) {
        try {
            OutputStream outputStream = socket.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.write(
                    ProtocolUtils.encode(
                            List.of("PING")
                    ).getBytes()
            );
        } catch (IOException e) {
            log.error("Could not ping master. Handshake failed...");
        } catch (Exception e) {
            log.error("Unexpected exception while pinging master node. Handshake failed...");
        }
    }
}
