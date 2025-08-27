package parser.strategies;

import lombok.*;
import parser.Parser;
import parser.ParserStrategy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ArrayStrategy implements ParserStrategy {
    private Parser parser;

    @Override
    public Object parse(ByteBuffer buffer) throws IOException {
        var elements = Parser.readNumber(buffer);

        if (elements == -1) {
            return null;
        }

        List<Object> parsedArray = new ArrayList<>(elements);

        for (int i = 0; i < elements; i++) {
            var element = parser.parse(buffer);
            parsedArray.add(element);
        }

        return parsedArray;
    }
}
