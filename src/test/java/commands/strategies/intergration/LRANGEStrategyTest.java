package commands.strategies.intergration;

import commands.RedisTestContainer;
import io.lettuce.core.RedisCommandExecutionException;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static commands.Errors.WRONG_TYPE;
import static org.junit.jupiter.api.Assertions.*;

public class LRANGEStrategyTest extends RedisTestContainer {
    private static RedisCommands<String, String> client;
    private static final String KEY = "listKey";
    private static final List<String> TEST_VALUES = List.of("one", "two", "three", "four", "five");

    @BeforeEach
    void init() {
        client = registerClient();

        client.del(KEY);
        client.rpush(KEY, TEST_VALUES.toArray(String[]::new));
    }

    @ParameterizedTest(name = "LRANGE with start={0} and stop={1}")
    @MethodSource("rangeArguments")
    void shouldReturnCorrectSubRange(int start, int stop, List<String> expectedList) {
        // when
        List<String> result = client.lrange(KEY, start, stop);

        // then
        assertEquals(expectedList, result);
    }

    private static Stream<Arguments> rangeArguments() {
        return Stream.of(
                Arguments.of(0, 4, TEST_VALUES),
                Arguments.of(0, 2, List.of("one", "two", "three")),
                Arguments.of(1, 3, List.of("two", "three", "four")),
                Arguments.of(-3, -1, List.of("three", "four", "five")),
                Arguments.of(10, 12, Collections.emptyList()),
                Arguments.of(3, 1, Collections.emptyList())

        );
    }

    @Test
    void shouldReturnEmptyListForNonExistentKey() {
        // given
        var nonExistentKey = "nonExistentKey";

        // when
        List<String> result = client.lrange(nonExistentKey, 0, -1);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFailWithWrongType() {
        // given
        client.set(KEY, "someStringValue");

        // then
        assertThrows(RedisCommandExecutionException.class,
                () -> client.lrange(KEY, 0, -1),
                WRONG_TYPE
        );
    }
}
