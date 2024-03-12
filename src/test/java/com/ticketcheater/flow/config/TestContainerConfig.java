package com.ticketcheater.flow.config;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class TestContainerConfig implements BeforeAllCallback {

    private static final String REDIS_IMAGE = "redis";
    private static final int REDIS_PORT = 6379;
    private GenericContainer redis;

    @Override
    public void beforeAll(ExtensionContext context) {
        redis = new GenericContainer(DockerImageName.parse(REDIS_IMAGE))
                .withExposedPorts(REDIS_PORT);
        redis.start();
        System.setProperty("spring.data.redis.host", redis.getHost());
        System.setProperty("spring.data.redis.flow.port", String.valueOf(redis.getMappedPort(REDIS_PORT)));
        System.setProperty("spring.data.redis.auth.port", String.valueOf(redis.getMappedPort(REDIS_PORT)));
    }

}
