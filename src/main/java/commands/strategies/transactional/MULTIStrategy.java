package commands.strategies.transactional;

import commands.transaction.TransactionalClientManager;
import commands.transaction.TransactionalCommandStrategy;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

import static commands.Errors.NESTED_TRANSACTIONS_ERROR;
import static commands.Errors.checkArgNumber;
import static commands.ProtocolUtils.OK;
import static commands.ProtocolUtils.encodeSimpleError;

@AllArgsConstructor
@Slf4j
public class MULTIStrategy implements TransactionalCommandStrategy {

    private final TransactionalClientManager clientManager;

    @Override
    public ByteBuffer execute(List<String> args, SocketChannel channel) {

        var err = checkArgNumber(args, 0, 0);
        if (err != null) {
            return err;
        }

        if (clientManager.isInTransaction(channel)) {
            return ByteBuffer.wrap(
                    encodeSimpleError(NESTED_TRANSACTIONS_ERROR).getBytes()
            );
        }

        clientManager.createTransaction(channel);
        return ByteBuffer.wrap(OK.getBytes());
    }
}
