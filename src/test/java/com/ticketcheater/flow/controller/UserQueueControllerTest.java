package com.ticketcheater.flow.controller;

import com.ticketcheater.flow.service.UserQueueService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.mockito.Mockito.when;

@DisplayName("컨트롤러 - 대기열")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserQueueControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    UserQueueService userQueueService;

    @DisplayName("올바른 토큰을 가진 유저가 대기 큐에 있는지를 요청하면 참을 반환한다")
    @Test
    void givenUserWithValidToken_whenRequesting_thenReturnsTrue() {
        String queue = "default";
        Long userId = 100L;
        String token = "d333a5d4eb24f3f5cdd767d79b8c01aad3cd73d3537c70dec430455d37afe4b8";

        when(userQueueService.isAllowedByToken(queue, userId, token)).thenReturn(Mono.just(true));

        webTestClient.get()
                .uri(String.format("flow/allowed?queue=%s&user_id=%d&token=%s", queue, userId, token))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.result.allowed").isEqualTo(true);
    }

    @DisplayName("올바르지 않은 토큰을 가진 유저가 대기 큐에 있는지를 요청하면 거짓을 반환한다")
    @Test
    void givenUserWithInValidToken_whenRequesting_thenReturnsFalse() {
        String queue = "default";
        Long userId = 100L;
        String token = "d333a5d4eb24f3f5cdd767d79b8c01aad3cd73d3537c70dec430455d37afe4b8";

        when(userQueueService.isAllowedByToken(queue, userId, token)).thenReturn(Mono.just(false));

        webTestClient.get()
                .uri(String.format("flow/allowed?queue=%s&user_id=%d&token=%s", queue, userId, token))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.result.allowed").isEqualTo(false);
    }

    @DisplayName("대기 큐에 있는 유저들의 우선순위를 정상적으로 반환한다")
    @Test
    void givenUserRegisteredInQueue_whenRequesting_thenReturnsRank() {
        String queue = "default";
        Long userId = 100L;

        when(userQueueService.getRank(queue, userId)).thenReturn(Mono.just(1L));

        webTestClient.get()
                .uri(String.format("/flow/rank?queue=%s&user_id=%d", queue, userId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.result.rank").isEqualTo(1L);
    }

    @DisplayName("대기 큐에 없는 유저들의 우선순위를 -1로 반환한다")
    @Test
    void givenUserNotRegisteredInQueue_whenRequesting_thenReturnsMinusOne() {
        String queue = "default";
        Long userId = 100L;

        when(userQueueService.getRank(queue, userId)).thenReturn(Mono.just(-1L));

        webTestClient.get()
                .uri(String.format("/flow/rank?queue=%s&user_id=%d", queue, userId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.result.rank").isEqualTo(-1L);
    }

    @DisplayName("토큰을 정상적으로 반환한다")
    @Test
    void givenUser_whenRequesting_thenReturnsToken() {
        String queue = "default";
        Long userId = 100L;

        when(userQueueService.generateToken(queue, userId)).thenReturn(Mono.just("token"));

        webTestClient.get()
                .uri(String.format("/flow/touch?queue=%s&user_id=%d", queue, userId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectCookie()
                .maxAge(String.format("user-queue-%s-token", queue), Duration.ofSeconds(300))
                .expectCookie()
                .path(String.format("user-queue-%s-token", queue), "/")
                .expectBody()
                .jsonPath("$.result.token").isEqualTo("token");
    }

}
