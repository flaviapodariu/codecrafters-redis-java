package commands.strategies;

import commands.RedisTestContainer;
import io.lettuce.core.XReadArgs;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DELStrategyTest extends RedisTestContainer {

    private static final String KEY_STRING = "stringKey";
    private static final String KEY_LIST = "listKey";
    private static final String KEY_STREAM = "streamKey";

    private RedisCommands<String, String> client;

    @BeforeEach
    void init() {
        client = registerClient();
    }

    @Test
    void shouldDeleteString() {
        // given
        client.set(KEY_STRING, "stringValue");

        // when
        long deletedCount = client.del(KEY_STRING);

        // then
        assertEquals(1, deletedCount);
        assertNull(client.get(KEY_STRING));
    }

    @Test
    void shouldDeleteList() {
        // given
        client.rpush(KEY_LIST, "item1", "item2");

        // when
        long deletedCount = client.del(KEY_LIST);

        // then
        assertEquals(1, deletedCount);
        assertEquals(0, client.llen(KEY_LIST));
    }

    @Test
    void shouldDeleteStream() {
        // given
        client.xadd(KEY_STREAM, "field", "value");

        // when
        long deletedCount = client.del(KEY_STREAM);

        // then
        assertEquals(1, deletedCount);
        assertTrue(client.xread(XReadArgs.StreamOffset.latest(KEY_STREAM)).isEmpty());
    }

    @Test
    void shouldDeleteDifferentTypes() {
        // given
        client.set(KEY_STRING, "stringValue");
        client.rpush(KEY_LIST, "item");
        client.xadd(KEY_STREAM, "field", "value");

        // when
        long deletedCount = client.del(KEY_STRING, KEY_LIST, KEY_STREAM);

        // then
        assertEquals(3, deletedCount);
        assertNull(client.get(KEY_STRING));
        assertEquals(0, client.llen(KEY_LIST));
        assertTrue(client.xread(XReadArgs.StreamOffset.latest(KEY_STREAM)).isEmpty());
    }

    @Test
    void shouldIgnoreNonExistentKey() {
        // given
        // No key is created

        // when
        long deletedCount = client.del("nonExistentKey");

        // then
        assertEquals(0, deletedCount);
    }

    @Test
    void shouldDeleteExistingKeysAndIgnoreNonExistentKeys() {
        // given
        client.set(KEY_STRING, "stringValue");
        client.rpush(KEY_LIST, "item");

        // when
        long deletedCount = client.del(KEY_STRING, "nonExistentKey", KEY_LIST);

        // then
        assertEquals(2, deletedCount);
        assertNull(client.get(KEY_STRING));
        assertEquals(0, client.llen(KEY_LIST));
    }
}
