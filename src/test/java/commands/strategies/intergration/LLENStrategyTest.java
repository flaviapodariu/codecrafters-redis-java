package commands.strategies.intergration;

import commands.RedisTestContainer;
import io.lettuce.core.RedisCommandExecutionException;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static commands.Errors.WRONG_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LLENStrategyTest extends RedisTestContainer {
    private static RedisCommands<String, String> client;
    private static final String KEY = "listKey";

    @BeforeEach
    void init() {
        client = registerClient();
        client.del(KEY);
    }

    @Test
    void shouldReturnCorrectLengthForPopulatedList() {
        // given
        client.lpush(KEY, "one", "two", "three");

        // when
        Long length = client.llen(KEY);

        // then
        assertEquals(3L, length);
    }

    @Test
    void shouldReturnZeroForNonExistentKey() {
        // when
        Long length = client.llen("nonExistentKey");

        // then
        assertEquals(0L, length);
    }

    @Test
    void shouldReturnZeroForEmptyList() {
        // given
        client.lpush(KEY, "randomValue");
        client.lpop(KEY);
        // when
        Long length = client.llen(KEY);

        // then
        assertEquals(0L, length);
    }

    @Test
    void shouldFailWithWrongType() {
        // given
        client.set(KEY, "someStringValue");

        // then
        assertThrows(RedisCommandExecutionException.class,
                () -> client.llen(KEY),
                WRONG_TYPE
        );
    }
}
