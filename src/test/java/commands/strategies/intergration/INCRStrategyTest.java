package commands.strategies.intergration;

import commands.RedisTestContainer;
import io.lettuce.core.RedisCommandExecutionException;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static commands.Errors.NOT_AN_INTEGER;
import static commands.Errors.WRONG_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class INCRStrategyTest extends RedisTestContainer {
    private static RedisCommands<String, String> client;
    private static final String KEY = "key";


    @BeforeEach
    void init() {
        client = registerClient();
        client.del(KEY);
    }

    @Test
    void shouldSucceed() {
        client.set(KEY, "2");
        var incremented =  client.incr(KEY);
        assertEquals(3L, incremented);
    }

    @Test
    void shouldCreateKey() {
        var incremented =  client.incr("nonExistentKey");
        assertEquals(1L, incremented);
    }

    @Test
    void shouldFailNonNumeric() {
        client.set(KEY, "value");
        assertThrows(RedisCommandExecutionException.class,
                ()-> client.incr(KEY),
                NOT_AN_INTEGER);
    }

    @Test
    void shouldFailOverflow() {
        client.set(KEY, "value");
        assertThrows(RedisCommandExecutionException.class,
                ()-> client.incr(KEY),
                NOT_AN_INTEGER);
    }
    @Test
    void shouldFailWrongType() {
        client.xadd(KEY, Map.of("key", "value"));
        assertThrows(RedisCommandExecutionException.class,
                ()-> client.incr(KEY),
                WRONG_TYPE);
    }
}
