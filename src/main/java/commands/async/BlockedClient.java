package commands.async;

import commands.Command;
import lombok.*;

import java.nio.channels.SocketChannel;
import java.time.Instant;
import java.util.List;


@AllArgsConstructor
@RequiredArgsConstructor
@Setter
@Getter
@Builder
public class BlockedClient {

    private SocketChannel channel;
    private Instant timeout;
    private List<String> keys;
    private List<String> ids;
    private Command executedCommand;
    private UnblockingMethod method;
}
