package server.info;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReplicationInfo {
    private String role = "master";
    private int connectedSlaves = 0;
    private String masterHost = "localhost";
    private int masterPort = 6379;
}
