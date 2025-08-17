package parser.strategies;

import lombok.Getter;
import lombok.Setter;
import parser.Parser;
import parser.ParserStrategy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Getter
@Setter
public class BulkStringStrategy implements ParserStrategy {

    @Override
    public Object parse(ByteBuffer buffer) throws IOException {
        var length = Parser.readNumber(buffer);
        if (length == -1) {
            return null;
        }

        if (buffer.remaining() < length + 2) { // +2 for CRLF
            // TODO what if buffer not complete? wait for complete buffer => blocking?
            throw new IOException("Not enough bytes in buffer for bulk string of length " + length);
        }

        byte[] bulkString = new byte[length];
        buffer.get(bulkString);

        if (buffer.get() != '\r' || buffer.get() != '\n') {
            throw new IOException("CRLF terminator incomplete or doesn't exist");
        }
        return new String(bulkString, StandardCharsets.UTF_8);
    }
}
