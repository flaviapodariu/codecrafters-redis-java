package parser;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface ParserStrategy {

    Object parse(ByteBuffer buffer) throws IOException;

}
