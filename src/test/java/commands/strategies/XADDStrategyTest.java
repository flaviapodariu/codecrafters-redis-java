package commands.strategies;

import commands.ProtocolUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import store.KeyValueStore;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class XADDStrategyTest {

    private static XADDStrategy strategy;

    @BeforeAll
    static void init() {
        KeyValueStore kvStore = new KeyValueStore();
        strategy = new XADDStrategy(kvStore);
    }

    @ParameterizedTest
    @MethodSource("generateValidIds")
    void idValidationShouldSucceed(List<String> args, String expected) {
        var result = strategy.execute(args);
        String stringResult = StandardCharsets.UTF_8.decode(result).toString();
        String encodedExpected = ProtocolUtils.encode(expected);
        assertEquals(encodedExpected, stringResult);
    }

    private static Stream<Arguments> generateValidIds() {
        return Stream.of(
                Arguments.of(List.of("streamKey", "0-1", "keyVAL", "val"), "0-1"),
                Arguments.of(List.of("streamKey", "0-2", "keyVAL", "val"), "0-2"),
                Arguments.of(List.of("neverSeenKey", "1-2", "keyVAL", "val"), "1-2"),
                Arguments.of(List.of("otherKey", "1526919030473-*", "keyVAL", "val"), "1526919030473-0"),
                Arguments.of(List.of("otherKey", "1526919030473-*", "keyVAL", "val"), "1526919030473-1"),
                Arguments.of(List.of("K", "0-*", "keyVAL", "val"), "0-1")
        );
    }

}