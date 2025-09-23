import server.ArgumentParser;
import server.RedisServer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;

@Slf4j
public class Main {
  public static void main(String[] args){
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

        var argParser = new ArgumentParser();
        argParser.parseArgs(args);

        int port = argParser.getPort();
        String host = argParser.getHost();
        var client = new RedisServer(host, port);

        try {
            client.run();
        } catch (IOException e) {
            log.error("Error during server processing: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("An unexpected error occurred: {}", e.getMessage(), e);
        }

  }
}
