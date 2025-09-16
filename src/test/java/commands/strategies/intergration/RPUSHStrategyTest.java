package commands.strategies.intergration;

import commands.RedisTestContainer;
import io.lettuce.core.RedisCommandExecutionException;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static commands.Errors.WRONG_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RPUSHStrategyTest extends RedisTestContainer {
    private static RedisCommands<String, String> client;

    @BeforeEach
    void init() {
        client = registerClient();
    }

    @Test
    void shouldSucceedWithSingleValue() {
        // given
        var key = "singleKey";
        var value = "firstValue";

        // when
        Long newLength = client.rpush(key, value);

        // then
        assertEquals(1L, newLength);
        assertEquals(List.of(value), client.lrange(key, 0, 0));
    }

    @Test
    void shouldSucceedWithMultipleValues() {
        // given
        var key = "multipleValues";
        var value1 = "value1";
        var value2 = "value2";
        var value3 = "value3";

        // when
        Long newLength = client.rpush(key, value1, value2, value3);

        // then
        assertEquals(3L, newLength);
        assertEquals(List.of(value1, value2, value3), client.lrange(key, 0, -1));
    }

    @Test
    void shouldReturnCorrectNumberOfElements() {
        // given
        var key = "correctElements";
        client.rpush(key, "a", "b");

        // when
        Long newLength = client.rpush(key, "c", "d");

        // then
        assertEquals(4L, newLength);
        assertEquals(List.of("a", "b", "c", "d"), client.lrange(key, 0, -1));
    }

    @Test
    void shouldFailWithWrongType() {
        // given
        var key = "stringKey";
        client.set(key, "someStringValue");

        // then
        assertThrows(RedisCommandExecutionException.class,
                () -> client.rpush(key, "listArg"),
                WRONG_TYPE
        );
    }
}
