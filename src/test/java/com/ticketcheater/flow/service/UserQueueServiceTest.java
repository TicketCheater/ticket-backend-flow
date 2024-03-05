package com.ticketcheater.flow.service;

import com.ticketcheater.flow.config.TestContainerConfig;
import com.ticketcheater.flow.exception.TicketApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.test.StepVerifier;

@DisplayName("비즈니스 로직 - 대기열")
@ExtendWith(TestContainerConfig.class)
@SpringBootTest
class UserQueueServiceTest {

    @Autowired
    UserQueueService sut;

    @Autowired
    ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @BeforeEach
    void beforeEach() {
        ReactiveRedisConnection redisConnection = reactiveRedisTemplate.getConnectionFactory().getReactiveConnection();
        redisConnection.serverCommands().flushAll().subscribe();
    }

    @DisplayName("대기 큐에 정상적으로 등록한다")
    @Test
    void registerWaitQueue() {
        StepVerifier.create(sut.registerWaitQueue("default", 100L))
                .expectNext(1L)
                .verifyComplete();

        StepVerifier.create(sut.registerWaitQueue("default", 101L))
                .expectNext(2L)
                .verifyComplete();

        StepVerifier.create(sut.registerWaitQueue("default", 102L))
                .expectNext(3L)
                .verifyComplete();
    }

    @DisplayName("이미 대기 큐에 있으면 오류를 내뱉는다")
    @Test
    void alreadyRegisterWaitQueue() {
        StepVerifier.create(sut.registerWaitQueue("default", 100L))
                .expectNext(1L)
                .verifyComplete();

        StepVerifier.create(sut.registerWaitQueue("default", 100L))
                .expectError(TicketApplicationException.class)
                .verify();
    }

    @DisplayName("대기 큐에 아무도 없으면 진행 큐에 아무도 등록하지 않는다")
    @Test
    void emptyAllowUser() {
        StepVerifier.create(sut.allowUser("default"))
                .expectNext(0L)
                .verifyComplete();
    }

    @DisplayName("대기 큐에 있는 유저들을 진행 큐에 정상적으로 등록한다")
    @Test
    void allowUser() {
        StepVerifier.create(
                        sut.registerWaitQueue("default", 100L)
                                .then(sut.registerWaitQueue("default", 101L))
                                .then(sut.registerWaitQueue("default", 102L))
                                .then(sut.allowUser("default")))
                .expectNext(3L)
                .verifyComplete();
    }

    @DisplayName("대기 큐에 있는 유저들을 진행 큐에 정상적으로 등록한 뒤 다시 대기 큐에 정상적으로 등록한다")
    @Test
    void allowUserAfterRegisterWaitQueue() {
        StepVerifier.create(
                        sut.registerWaitQueue("default", 100L)
                                .then(sut.registerWaitQueue("default", 101L))
                                .then(sut.registerWaitQueue("default", 102L))
                                .then(sut.allowUser("default"))
                                .then(sut.registerWaitQueue("default", 200L)))
                .expectNext(1L)
                .verifyComplete();
    }

    @DisplayName("대기 큐에 있는 유저들의 우선순위를 정상적으로 반환한다")
    @Test
    void getRank() {
        StepVerifier.create(sut.registerWaitQueue("default", 100L)
                        .then(sut.getRank("default", 100L)))
                .expectNext(1L)
                .verifyComplete();
    }

    @DisplayName("대기 큐에 없는 유저들의 우선순위를 -1로 반환한다")
    @Test
    void emptyRank() {
        StepVerifier.create(sut.getRank("default", 100L))
                .expectNext(-1L)
                .verifyComplete();
    }

    @DisplayName("올바른 토큰을 가진 유저가 대기 큐에 있는지를 요청하면 참을 반환한다")
    @Test
    void isAllowedByToken() {
        StepVerifier.create(sut.isAllowedByToken("default", 100L, "d333a5d4eb24f3f5cdd767d79b8c01aad3cd73d3537c70dec430455d37afe4b8"))
                .expectNext(true)
                .verifyComplete();
    }

    @DisplayName("올바르지 않은 토큰을 가진 유저가 대기 큐에 있는지를 요청하면 거짓을 반환한다")
    @Test
    void isNotAllowedByToken() {
        StepVerifier.create(sut.isAllowedByToken("default", 100L, ""))
                .expectNext(false)
                .verifyComplete();
    }

    @DisplayName("토큰을 정상적으로 반환한다")
    @Test
    void generateToken() {
        StepVerifier.create(sut.generateToken("default", 100L))
                .expectNext("d333a5d4eb24f3f5cdd767d79b8c01aad3cd73d3537c70dec430455d37afe4b8")
                .verifyComplete();
    }

}
