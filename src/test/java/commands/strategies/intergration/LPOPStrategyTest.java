package commands.strategies.intergration;

import commands.RedisTestContainer;
import io.lettuce.core.RedisCommandExecutionException;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static commands.Errors.WRONG_TYPE;
import static org.junit.jupiter.api.Assertions.*;

public class LPOPStrategyTest extends RedisTestContainer {
    private static RedisCommands<String, String> client;
    private static final String KEY = "listKey";

    @BeforeEach
    void init() {
        client = registerClient();
        client.del(KEY);
    }

    @Test
    void shouldPopSingleItem() {
        // given
        client.rpush(KEY, "a", "b", "c");

        // when
        String popped = client.lpop(KEY);

        // then
        assertEquals("a", popped);
        assertEquals(2L, client.llen(KEY));
        assertEquals(List.of("b", "c"), client.lrange(KEY, 0, -1));
    }

    @Test
    void shouldPopMultipleItems() {
        // given
        client.rpush(KEY, "a", "b", "c", "d", "e");

        // when
        List<String> popped = client.lpop(KEY, 3);

        // then
        assertEquals(3, popped.size());
        assertEquals(List.of("a", "b", "c"), popped);
        assertEquals(2L, client.llen(KEY));
        assertEquals(List.of("d", "e"), client.lrange(KEY, 0, -1));
    }

    @Test
    void shouldReturnNullForNonExistentKey() {
        // when
        String popped = client.lpop(KEY);

        // then
        assertNull(popped);
    }

    @Test
    void shouldReturnNullForEmptyList() {
        // given
        client.lpush(KEY, "value");
        client.lpop(KEY);

        // when
        String popped = client.lpop(KEY);

        // then
        assertNull(popped);
        assertEquals(0L, client.llen(KEY));
    }

    @Test
    void shouldFailWithWrongType() {
        // given
        client.set(KEY, "a string value");

        // then
        assertThrows(RedisCommandExecutionException.class,
                () -> client.lpop(KEY),
                WRONG_TYPE
        );
    }
}
