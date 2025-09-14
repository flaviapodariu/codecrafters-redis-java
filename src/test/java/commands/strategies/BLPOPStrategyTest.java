package commands.strategies;

import commands.RedisTestContainer;
import io.lettuce.core.RedisCommandExecutionException;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static commands.Errors.WRONG_TYPE;
import static org.junit.jupiter.api.Assertions.*;

public class BLPOPStrategyTest extends RedisTestContainer {
    private static RedisCommands<String, String> client;
    private static final String LIST_KEY = "list";

    @BeforeEach
    void init() {
        client = registerClient();
        client.del(LIST_KEY);
    }

    @Test
    void shouldReturnCorrectItemImmediatelyWhenKeyExists() {
        // given
        client.rpush(LIST_KEY, "item1", "item2");

        // when
        var result = client.blpop(5, LIST_KEY);

        // then
        assertEquals(LIST_KEY, result.getKey());
        assertEquals("item1", result.getValue());
    }

    @Test
    void shouldBlockAndReturnWhenItemIsAdded() throws Exception {
        // given
        var future = CompletableFuture.supplyAsync(
                () -> client.blpop(5, LIST_KEY)
        );

        // when
        var anotherClient = registerClient();
        anotherClient.rpush(LIST_KEY, "newItem");

        // then
        var result = future.get(1, TimeUnit.SECONDS);
        assertEquals(LIST_KEY, result.getKey());
        assertEquals("newItem", result.getValue());
    }

    @Test
    void shouldReturnInOrder() throws Exception {
        // given
        var firstClientFuture = CompletableFuture.supplyAsync(
                () -> client.blpop(5, LIST_KEY)
        );

        var secondClient = registerClient();
        var secondClientFuture = CompletableFuture.supplyAsync(
                () -> secondClient.blpop(0.5 , LIST_KEY)
        );

        // when
        var writerClient = registerClient();
        writerClient.rpush(LIST_KEY, "newItem");

        // then
        var result = firstClientFuture.get(1, TimeUnit.SECONDS);
        assertEquals(LIST_KEY, result.getKey());
        assertEquals("newItem", result.getValue());

        var secondClientResult = secondClientFuture.get(1, TimeUnit.SECONDS);
        assertNull(secondClientResult);

    }


    @Test
    void shouldBlockAndTimeoutWhenNoItemIsAdded() {
        // when
        var result = client.blpop(1, LIST_KEY);

        // then
        assertNull(result);
    }

    // TODO BLPOP with multiple keys
    @Disabled
    @Test
    void shouldReturnTheFirstAvailableList() {
        // given
        client.rpush("keyA", "a");
        client.rpush("keyC", "c");

        // when
        var result = client.blpop(5, "keyB", "keyC", "keyA");

        // then
        assertEquals("keyA", result.getKey());
        assertEquals("a", result.getValue());
    }

    @Test
    void shouldFailWithWrongType() {
        // given
        client.set(LIST_KEY, "value");

        // then
        assertThrows(RedisCommandExecutionException.class,
                () -> client.blpop(5, LIST_KEY),
                WRONG_TYPE
        );
    }
}
