package commands;

import commands.strategies.EchoStrategy;
import commands.strategies.PingStrategy;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

public class Command {

    private final Map<String, CommandStrategy> strategies = Map.of(
            "PING", new PingStrategy(),
            "ECHO", new EchoStrategy()
    );


    public ByteBuffer execute(List<String> args) {
        var command = args.getFirst();
        var strategy = strategies.getOrDefault(command, null);

        if (strategy == null) {
            throw new IllegalArgumentException(String.format("Command %s does not exist", command));
        }

        return strategy.execute(args);
    }
}
