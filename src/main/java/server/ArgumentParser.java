package server;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@NoArgsConstructor
@Slf4j
public class ArgumentParser {

    private int port = 6379;
    private String host = "0.0.0.0";

    public void parseArgs(String[] args) {
        for (int i = 0; i < args.length - 1; i += 2) {
            if (args[i].startsWith("--")) {
                String flag = args[i].substring(2);
                switch (flag.toLowerCase()) {
                    case "port" -> {
                        this.port = parseInt(args[1]);
                    }
                    case "host" -> {

                    }
                }
            }
        }
    }

    private int parseInt(String argValue) {
        int number = -1;
        try {
            number = Integer.parseInt(argValue);
        } catch (NumberFormatException nfe) {
            log.error("Argument value could not be parsed as integer...");
        }
        return number;
    }

}
