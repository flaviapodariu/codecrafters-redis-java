package commands;

import commands.strategies.*;
import store.KeyValueStore;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

public class Command {

    private final Map<String, CommandStrategy> strategies;

    public Command(KeyValueStore kvStore) {
        this.strategies = Map.of(
                "PING", new PingStrategy(),
                "ECHO", new EchoStrategy(),
                "GET", new GetStrategy(kvStore),
                "SET", new SetStrategy(kvStore),
                "RPUSH", new RpushStrategy(kvStore),
                "LRANGE", new LrangeStrategy(kvStore)
        );
    }

    public ByteBuffer execute(List<String> args) {
        var command = args.getFirst().toUpperCase();

        var strategy = strategies.getOrDefault(command, null);
        if (strategy == null) {
            throw new IllegalArgumentException(String.format("Command %s does not exist", command));
        }

        return strategy.execute(args.subList(1, args.size()));
    }
}
