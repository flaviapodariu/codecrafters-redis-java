package commands.strategies.intergration;

import commands.RedisTestContainer;
import io.lettuce.core.RedisCommandExecutionException;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static commands.Errors.WRONG_TYPE;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GETStrategyTest extends RedisTestContainer {
    private static RedisCommands<String, String> client;
    private static final String KEY = "key";


    @BeforeEach
    void init() {
        client = registerClient();
        client.del(KEY);
    }

    @Test
    void shouldSucceed() {
        // given
        var value = "value";

        // when
        client.set(KEY, value);
        var result = client.get(KEY);
        // then
        Assertions.assertEquals(value, result);
    }

    @Test
    void shouldReturnNil() {
        // when
        var result = client.get(KEY);
        // then
        Assertions.assertNull(result);
    }

    @Test
    void shouldFailWrongType() {
        // when
        client.lpush(KEY, "listArg");
        // then
        assertThrows(RedisCommandExecutionException.class,
                () -> client.get(KEY),
                WRONG_TYPE
        );
    }

}
