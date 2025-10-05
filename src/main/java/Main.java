import server.Configuration;
import server.RedisServer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static server.Configuration.*;

@Slf4j
public class Main {
  public static void main(String[] args){

        var nodeConfig = new Configuration();
        var parsedArgs = parseArgs(args);

        try {
            configureServer(nodeConfig, parsedArgs);
        } catch (NumberFormatException e) {
            log.error("Invalid port number!", e);
        }

        var client = new RedisServer(nodeConfig);

        try {
            client.run();
        } catch (IOException e) {
            log.error("Error during server processing: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("An unexpected error occurred: {}", e.getMessage(), e);
        }

  }

  private static Map<String, String> parseArgs(String[] args) {
      Map<String, String> result = new HashMap<>();

      for (int i = 0; i < args.length; i += 2) {
          if (args[i].startsWith("--")) {
              result.put(args[i].substring(2), args[i+1]);
          }
      }
      return result;
  }

  private static void configureServer(Configuration nodeConfig, Map<String, String> customProperties) {
      customProperties.forEach( (k, v) -> {
          switch (k) {
              case "port" -> nodeConfig.getServer().put(TCP_PORT, v);
              case "replicaof" -> {
                  var masterInfo = v.split(" ");
                  var host = masterInfo[0];
                  var port = masterInfo[1];
                  var connSlaves = Integer.parseInt(
                          nodeConfig.getReplication().get(CONNECTED_SLAVES)
                  ) + 1;

                  nodeConfig.getReplication().put(ROLE, "slave");
                  nodeConfig.getReplication().put(CONNECTED_SLAVES, String.valueOf(connSlaves));
                  nodeConfig.getReplication().put(MASTER_HOST, host);
                  nodeConfig.getReplication().put(MASTER_PORT, port);
              }
          }
      });

  }
}
