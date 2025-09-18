package commands.strategies.transactional;

import commands.transaction.TransactionalClientManager;
import commands.transaction.TransactionalCommandStrategy;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

import static commands.Errors.DISCARD_WITHOUT_TRANSACTION;
import static commands.ProtocolUtils.OK;
import static commands.ProtocolUtils.encodeSimpleError;

@AllArgsConstructor
@Slf4j
public class DISCARDStrategy implements TransactionalCommandStrategy {

    private final TransactionalClientManager clientManager;

    @Override
    public ByteBuffer execute(List<String> args, SocketChannel channel) {


        if (this.clientManager.isInTransaction(channel)) {
            this.clientManager.removeTransaction(channel);
            return ByteBuffer.wrap(OK.getBytes());
        }

        return ByteBuffer.wrap(
                encodeSimpleError(DISCARD_WITHOUT_TRANSACTION).getBytes()
        );
    }
}
