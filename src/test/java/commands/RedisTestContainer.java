package commands;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.protocol.ProtocolVersion;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Paths;
import java.time.Duration;

@Testcontainers
@Slf4j
public class RedisTestContainer {
    protected static GenericContainer<?> redis;
    private static final int REDIS_PORT = 6379;

    @BeforeAll
    static void startRedisContainer() {
        ImageFromDockerfile customImage = new ImageFromDockerfile()
                .withFileFromPath(".", Paths.get("."));

        redis = new GenericContainer<>(customImage)
                .withExposedPorts(REDIS_PORT)
                .withLogConsumer(new Slf4jLogConsumer(log))
                .waitingFor(Wait.forListeningPort())
                .withStartupTimeout(Duration.ofMinutes(2));

        redis.start();
    }

    @AfterAll
    static void stopRedisContainer() {
        if (redis != null) {
            redis.stop();
        }
    }

    public static RedisCommands<String, String> registerClient() {
        String host = redis.getHost();
        Integer port = redis.getMappedPort(6379);

        RedisURI uri = RedisURI.builder()
                .withHost(host)
                .withPort(port)
                .withTimeout(Duration.ofSeconds(60))
                .build();

        ClientOptions clientOptions = ClientOptions.builder()
                .protocolVersion(ProtocolVersion.RESP2)
                .pingBeforeActivateConnection(true)
                .build();

        RedisClient lettuceClient = RedisClient.create(uri);
        lettuceClient.setOptions(clientOptions);

        StatefulRedisConnection<String, String> connection = lettuceClient.connect();
        return connection.sync();
    }
}