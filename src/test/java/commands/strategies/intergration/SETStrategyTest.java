package commands.strategies.intergration;

import commands.RedisTestContainer;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class SETStrategyTest extends RedisTestContainer {
    private static RedisCommands<String, String> client;

    @BeforeEach
    void init() {
        client = registerClient();
    }

    @Test
    void shouldSucceedSimpleSet() {
        // given
        var key = "key";
        var value = "value";

        // when
        var result = client.set(key, value);

        // then
        Assertions.assertEquals("OK", result);
        Assertions.assertEquals(value, client.get(key));
    }


    @Test
    void shouldSucceedWithExpiryEX() throws InterruptedException {
        // given
        var key = "exKey";
        var value = "exValue";
        var expiryInSeconds = 1L;

        // when
        var result = client.set(key, value, SetArgs.Builder.ex(expiryInSeconds));

        // then
        Assertions.assertEquals("OK", result);
        Assertions.assertEquals(value, client.get(key));

        Thread.sleep(TimeUnit.SECONDS.toMillis(expiryInSeconds + 1));
        Assertions.assertNull(client.get(key));
    }

    @Test
    void shouldSucceedWithExpiryPX() throws InterruptedException {
        // given
        var key = "pxKey";
        var value = "pxValue";
        var expiryInMilliseconds = 100L;

        // when
        var result = client.set(key, value, SetArgs.Builder.px(expiryInMilliseconds));

        // then
        Assertions.assertEquals("OK", result);
        Assertions.assertEquals(value, client.get(key));

        Thread.sleep(expiryInMilliseconds + 50);
        Assertions.assertNull(client.get(key));
    }

    @Test
    void shouldSucceedWithExpiryEXAT() throws InterruptedException {
        // given
        var key = "exatKey";
        var value = "exatValue";
        long futureTimestamp = Instant.now().plusSeconds(1).getEpochSecond();

        // when
        var result = client.set(key, value, SetArgs.Builder.exAt(futureTimestamp));

        // then
        Assertions.assertEquals("OK", result);
        Assertions.assertEquals(value, client.get(key));

        Thread.sleep(TimeUnit.SECONDS.toMillis(1) + 50);
        Assertions.assertNull(client.get(key));
    }

    @Test
    void shouldSucceedWithExpiryPXAT() throws InterruptedException {
        // given
        var key = "pxatKey";
        var value = "pxatValue";
        long futureTimestamp = Instant.now().plusMillis(100).toEpochMilli();

        // when
        var result = client.set(key, value, SetArgs.Builder.pxAt(futureTimestamp));

        // then
        Assertions.assertEquals("OK", result);
        Assertions.assertEquals(value, client.get(key));

        Thread.sleep(150);

        Assertions.assertNull(client.get(key));
    }

    @Test
    void shouldSucceedWithNXWhenKeyNotExists() {
        // given
        var key = "newKey";
        var value = "newValue";

        // when
        var result = client.set(key, value, SetArgs.Builder.nx());

        // then
        Assertions.assertEquals("OK", result);
        Assertions.assertEquals(value, client.get(key));
    }

    @Test
    void shouldFailWithNXWhenKeyExists() {
        // given
        var key = "existingkey";
        var originalValue = "original";
        var newValue = "new";
        client.set(key, originalValue);

        // when
        var result = client.set(key, newValue, SetArgs.Builder.nx());

        // then
        Assertions.assertNull(result);
        Assertions.assertEquals(originalValue, client.get(key));
    }

    @Test
    void shouldSucceedWithXXWhenKeyExists() {
        // given
        var key = "existingkey";
        var originalValue = "original";
        var newValue = "new";
        client.set(key, originalValue);

        // when
        var result = client.set(key, newValue, SetArgs.Builder.xx());

        // then
        Assertions.assertEquals("OK", result);
        Assertions.assertEquals(newValue, client.get(key));
    }

    @Test
    void shouldFailWithXXWhenKeyNotExists() {
        // given
        var key = "nonexistingkey";
        var value = "value";

        // when
        var result = client.set(key, value, SetArgs.Builder.xx());

        // then
        Assertions.assertNull(result);
        Assertions.assertNull(client.get(key));
    }
}
