package commands.strategies.misc;

import commands.CommandStrategy;
import commands.ProtocolUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import server.Configuration;

import java.nio.ByteBuffer;
import java.util.List;

import static commands.Errors.checkArgNumber;

@AllArgsConstructor
@Slf4j
public class INFOStrategy implements CommandStrategy {

    private final Configuration nodeConfiguration;

    @Override
    public ByteBuffer execute(List<String> args) {
        var err = checkArgNumber(args, 0, 1);
        if (err != null) {
            return err;
        }

        var fullConfig = nodeConfiguration.getFullConfig();
        if (args.isEmpty()) {
            return ByteBuffer.wrap(
                    ProtocolUtils.encodeFullConfiguration(fullConfig).getBytes()
            );
        }

        var infoSection = args.getFirst().toLowerCase();

        if (!fullConfig.containsKey(infoSection)) {
            return ByteBuffer.wrap(
                    ProtocolUtils.encode("").getBytes()
            );
        }

        var sectionConfig = fullConfig.get(infoSection);

        return ByteBuffer.wrap(
                ProtocolUtils.encodeConfigurationSection(sectionConfig).getBytes()
        );
    }
}
