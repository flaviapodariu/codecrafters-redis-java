package commands.strategies.transactional;

import commands.ProtocolUtils;
import commands.transaction.TransactionManager;
import commands.transaction.TransactionalClientManager;
import commands.transaction.TransactionalCommandStrategy;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

import static commands.Errors.EXEC_WITHOUT_TRANSACTION;
import static commands.Errors.checkArgNumber;

@AllArgsConstructor
@Slf4j
public class EXECStrategy implements TransactionalCommandStrategy {

    private final TransactionalClientManager clientManager;
    private final TransactionManager transactionManager;

    @Override
    public ByteBuffer execute(List<String> args, SocketChannel channel) {
        var err = checkArgNumber(args, 0, 0);
        if (err != null) {
            return err;
        }

        if (!clientManager.isInTransaction(channel)) {
            return ByteBuffer.wrap(
                    ProtocolUtils.encodeSimpleError(EXEC_WITHOUT_TRANSACTION).getBytes()
            );
        }

        var commands = clientManager.getCommands(channel);
        return this.transactionManager.onExecuteTransaction(commands, channel);
    }
}
