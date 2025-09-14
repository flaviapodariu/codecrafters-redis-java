package commands.strategies;

import commands.RedisTestContainer;
import io.lettuce.core.RedisCommandExecutionException;
import io.lettuce.core.StreamMessage;
import io.lettuce.core.XReadArgs;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static commands.Errors.WRONG_TYPE;
import static org.junit.jupiter.api.Assertions.*;

public class XREADStrategyTest extends RedisTestContainer {
    private static RedisCommands<String, String> client;
    private static final String KEY_A = "streamKeyA";
    private static final String KEY_B = "streamKeyB";

    @BeforeEach
    void init() {
        client = registerClient();
        client.del(KEY_A, KEY_B);
    }

    @Test
    void shouldReturnCorrectMessagesForSimpleStreams() {
        // when
        client.xadd(KEY_A, "field1A", "value1A");
        client.xadd(KEY_A, "field2A", "value2A");
        List<StreamMessage<String, String>> resultA =
                client.xread(XReadArgs.StreamOffset.from(KEY_A, "0-0"));

        // then
        assertEquals(2, resultA.size());
        assertEquals("value1A", Objects.requireNonNull(resultA.get(0).getBody()).get("field1A"));
        assertEquals("value2A", Objects.requireNonNull(resultA.get(1).getBody()).get("field2A"));
    }

    @Test
    void shouldReadFromMultipleStreams() {
        // when
        client.xadd(KEY_A, "fieldA", "valueA");
        client.xadd(KEY_B, "fieldB", "valueB");
        List<StreamMessage<String, String>> result = client.xread(
                XReadArgs.StreamOffset.from(KEY_A, "0"),
                XReadArgs.StreamOffset.from(KEY_B, "0")
        );

        // then
        assertEquals(2, result.size());
        assertEquals(KEY_A, result.get(0).getStream());
        assertEquals(KEY_B, result.get(1).getStream());
    }

    @Test
    void shouldReturnEmptyListForNonExistentKey() {
        // when
        List<StreamMessage<String, String>> result =
                client.xread(XReadArgs.StreamOffset.latest("nonExistent"));

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFailWithWrongType() {
        // given
        client.set(KEY_A, "value");

        // then
        assertThrows(RedisCommandExecutionException.class,
                () -> client.xread(XReadArgs.StreamOffset.latest(KEY_A)),
                WRONG_TYPE
        );
    }

    @Test
    void shouldBlockAndTimeoutWhenNoNewData() {
        // given
        String lastId = client.xadd(KEY_A, "fieldLast", "valueLast");

        // when
        long startTime = System.nanoTime();
        List<StreamMessage<String, String>> result = client.xread(
                XReadArgs.Builder.block(Duration.ofMillis(100)),
                XReadArgs.StreamOffset.from(KEY_A, lastId)
        );
        long endTime = System.nanoTime();
        long durationMillis = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        // then
        assertTrue(result.isEmpty());
        assertTrue(durationMillis >= 100);
    }
}
