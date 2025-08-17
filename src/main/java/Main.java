import commands.Command;
import lombok.extern.slf4j.Slf4j;
import parser.Parser;

import java.io.IOException;

@Slf4j
public class Main {
  public static void main(String[] args){
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

        int port = 6379;
        Parser parser = new Parser();
        Command executor = new Command();
        EventLoop eventLoop = new EventLoop(parser, executor);

        try {
            eventLoop.configure(port);
            eventLoop.run();
        } catch (IOException e) {
            log.error("Error during server processing: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("An unexpected error occurred: {}", e.getMessage(), e);
        }

  }
}
