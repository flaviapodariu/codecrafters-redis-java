package commands;

public enum Command {
    COMMAND("command"),
    PING("ping"),
    ECHO("echo"),
    GET("get"),
    SET("set"),
    DEL("del"),
    LRANGE("lrange"),
    LLEN("llen"),
    LPOP("lpop"),
    TYPE("type"),
    XRANGE("xrange"),
    BLPOP("blpop"),
    RPUSH("rpush"),
    LPUSH("lpush"),
    XREAD("xread"),
    XADD("xadd"),
    INCR("incr"),
    MULTI("multi"),
    EXEC("exec"),
    SUBSCRIBE("subscribe"),
    NO_COMMAND("no_command");

    private final String commandName;

    Command(String commandName) {
        this.commandName = commandName;
    }

    @Override
    public String toString() {
        return this.commandName;
    }

    public static Command fromString(String commandName) {
        for (Command command : Command.values()) {
            if (command.commandName.equalsIgnoreCase(commandName)) {
                return command;
            }
        }
        return null;
    }
}
