package commands.strategies.misc;

import commands.CommandStrategy;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.List;

import static commands.Errors.checkArgNumber;

@AllArgsConstructor
@Slf4j
public class INFOStrategy implements CommandStrategy {
    @Override
    public ByteBuffer execute(List<String> args) {
        var err = checkArgNumber(args, 0, 1);
        if (err != null) {
            return err;
        }

        var infoCategory = args.getFirst();


        return null;
    }
}
