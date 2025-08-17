package parser.strategies;

import parser.Parser;
import parser.ParserStrategy;

import java.io.IOException;
import java.nio.ByteBuffer;

public class SimpleStringStrategy implements ParserStrategy {

    @Override
    public Object parse(ByteBuffer buffer) throws IOException {
        return Parser.readNextPart(buffer);
    }
}
