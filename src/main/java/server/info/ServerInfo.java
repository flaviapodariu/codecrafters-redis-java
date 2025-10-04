package server.info;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ServerInfo {
    private int tcpPort = 6379;
    private String redisVersion;
}
