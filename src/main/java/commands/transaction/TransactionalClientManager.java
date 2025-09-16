package commands.transaction;

import lombok.Getter;
import lombok.Setter;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class TransactionalClientManager {
    private Map<SocketChannel, List<List<String>>> transactions;

    public TransactionalClientManager() {
        this.transactions = new HashMap<>();
    }

    public void createTransaction(SocketChannel channel) {
        this.transactions.put(channel, new LinkedList<>());
    }

    public void removeTransaction(SocketChannel channel) {
        this.transactions.remove(channel);
    }

    public boolean isInTransaction(SocketChannel channel) {
        return this.transactions.containsKey(channel);
    }

    public List<List<String>> getCommands(SocketChannel channel) {
        return this.transactions.get(channel);
    }
}
