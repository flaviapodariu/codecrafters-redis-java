package commands.strategies;

import commands.RedisTestContainer;
import io.lettuce.core.Range;
import io.lettuce.core.RedisCommandExecutionException;
import io.lettuce.core.StreamMessage;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static commands.Errors.WRONG_TYPE;
import static org.junit.jupiter.api.Assertions.*;

public class XRANGEStrategyTest extends RedisTestContainer {
    private static RedisCommands<String, String> client;
    private static final String KEY = "streamKey";

    @BeforeEach
    void init() {
        client = registerClient();
        client.del(KEY);
        // Add a few entries to the stream for testing
        client.xadd(KEY, "field1", "value1");
        client.xadd(KEY, "field2", "value2");
        client.xadd(KEY, "field3", "value3");
    }

    /**
     * Helper method to extract stream IDs for testing
     * @return a list of all stream IDs in the test key
     */
    private List<String> getStreamIds() {
        return client.xrange(KEY, Range.create("-", "+")).stream()
                .map(StreamMessage::getId)
                .collect(Collectors.toList());
    }

    /**
     * Test case to verify XRANGE returns the full stream when using '-' and '+'.
     */
    @Test
    void shouldReturnFullStreamWithMinAndMaxIds() {
        // when
        List<StreamMessage<String, String>> result = client.xrange(KEY, Range.create("-", "+"));

        // then
        assertEquals(3, result.size());
        assertEquals("value1", Objects.requireNonNull(result.get(0).getBody()).get("field1"));
        assertEquals("value2", Objects.requireNonNull(result.get(1).getBody()).get("field2"));
        assertEquals("value3", Objects.requireNonNull(result.get(2).getBody()).get("field3"));
    }

    /**
     * Test case for XRANGE with a specific valid range of IDs.
     */
    @Test
    void shouldReturnCorrectRange() {
        // given
        List<String> ids = getStreamIds();
        String startId = ids.get(0);
        String endId = ids.get(1);

        // when
        List<StreamMessage<String, String>> result = client.xrange(KEY, Range.create(startId, endId));

        // then
        assertEquals(2, result.size());
        assertEquals(startId, result.get(0).getId());
        assertEquals(endId, result.get(1).getId());
    }

    /**
     * Test case for XRANGE on a non-existent key.
     */
    @Test
    void shouldReturnEmptyListForNonExistentKey() {
        // when
        List<StreamMessage<String, String>> result = client.xrange("nonExistentKey", Range.create("-", "+"));

        // then
        assertTrue(result.isEmpty());
    }

    /**
     * Test case to ensure XRANGE fails with a wrong data type.
     */
    @Test
    void shouldFailWithWrongType() {
        // given
        client.set(KEY, "someStringValue");

        // then
        assertThrows(RedisCommandExecutionException.class,
                () -> client.xrange(KEY, Range.create("-", "+")),
                WRONG_TYPE
        );
    }
}
