package parser;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import parser.strategies.ArrayStrategy;
import parser.strategies.BulkStringStrategy;
import parser.strategies.SimpleStringStrategy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

@NoArgsConstructor
@Slf4j
public class Parser {
        private final Map<Byte, ParserStrategy> strategies = Map.of(
                (byte) '+', new SimpleStringStrategy(),
                (byte) '$', new BulkStringStrategy(),
                (byte) '*', new ArrayStrategy(this)
        );

        /**
         * This method is responsible for choosing a strategy
         * and delegating the parsing to the concrete implementation
         * @param buffer the raw buffer, as received
         * @return the specific type expected from each strategy
         * @throws IOException if any error occurs during processing
         */
        public Object parse(ByteBuffer buffer) throws IOException {

                byte typeByte = buffer.get();
                var strategy = strategies.getOrDefault(typeByte, null);
                Object parsed;

                if (strategy == null) {
                        throw new IllegalArgumentException("Invalid type byte. Input malformed");
                }

                try {
                    parsed = strategy.parse(buffer);
                } catch (IOException e) {
                    log.error("An error occurred while parsing the buffer {}", e.getMessage(), e);
                    throw new RuntimeException(e);
                }

                return parsed;
        }


        /**
         * Parses the next part of the buffer, until reaching the first CRLF.
         * The buffer's position is advanced after the first CRLF
         * @param buffer the received buffer
         * @return the next part of the buffer as a String
         * @throws IOException if the buffer is missing the CRLF terminator or if it is malformed.
         */
        public static String readNextPart(ByteBuffer buffer) throws IOException {
                StringBuilder sb = new StringBuilder();
                while (buffer.hasRemaining()) {
                        var currChar = (char) buffer.get();
                        if (currChar == '\r') {
                                if (buffer.hasRemaining() && buffer.get() == '\n') {
                                        return sb.toString();
                                }
                                throw new IOException("Malformed buffer, CR not followed by LF");
                        }
                        sb.append(currChar);
                }
                throw new IOException("Malformed buffer, CR not found");
        }

        /**
         * Gets the number after a type byte by using the readNextPart method
         * @param buffer the received buffer
         * @return the number
         * @throws IOException if any error occurs during processing
         */
        public static int readNumber(ByteBuffer buffer) throws IOException {
                var number = readNextPart(buffer);
                return Integer.parseInt(number);
        }
}
