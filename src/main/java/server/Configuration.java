package server;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import server.info.ClientsInfo;
import server.info.ReplicationInfo;
import server.info.ServerInfo;

@Getter
@Setter
@NoArgsConstructor
public class Configuration {
    private ClientsInfo clientsInfo;
    private ReplicationInfo replicationInfo;
    private ServerInfo serverInfo;
}
