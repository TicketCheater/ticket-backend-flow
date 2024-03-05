package com.ticketcheater.flow.controller;

import com.ticketcheater.flow.dto.*;
import com.ticketcheater.flow.service.UserQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@RequestMapping("/flow")
@RequiredArgsConstructor
public class UserQueueController {
    private final UserQueueService userQueueService;

    @GetMapping("/allowed")
    public Mono<Response<AllowedUserResponse>> isAllowedUser(
            @RequestParam(name = "queue") String queue,
            @RequestParam(name = "user_id") Long userId,
            @RequestParam(name = "token") String token
    ) {
        return userQueueService.isAllowedByToken(queue, userId, token)
                .map(AllowedUserResponse::new)
                .map(Response::success);
    }

    @GetMapping("/rank")
    public Mono<Response<RankResponse>> getRank(
            @RequestParam(name = "queue") String queue,
            @RequestParam(name = "user_id") Long userId
    ) {
        return userQueueService.getRank(queue, userId)
                .map(RankResponse::new)
                .map(Response::success);
    }

    @GetMapping("/touch")
    public Mono<Response<TokenResponse>> touch(
            @RequestParam(name = "queue") String queue,
            @RequestParam(name = "user_id") Long userId,
            ServerWebExchange exchange
    ) {
        Mono<String> tokenMono = Mono.defer(() -> userQueueService.generateToken(queue, userId));

        return tokenMono.flatMap(token -> {
                    ResponseCookie cookie = ResponseCookie
                            .from("user-queue-%s-token".formatted(queue), token)
                            .maxAge(Duration.ofSeconds(300))
                            .path("/")
                            .build();
                    exchange.getResponse().addCookie(cookie);

                    return Mono.just(token);
                })
                .map(TokenResponse::new)
                .map(Response::success);
    }

    @GetMapping("/waiting-room")
    Mono<Response<WaitingResponse>> waitingRoom(
            @RequestParam(name = "queue") String queue,
            @RequestParam(name = "user_id") Long userId,
            ServerWebExchange exchange
    ) {
        String key = "user-queue-%s-token".formatted(queue);
        HttpCookie cookieValue = exchange.getRequest().getCookies().getFirst(key);
        String token = (cookieValue == null) ? "" : cookieValue.getValue();

        return userQueueService.isAllowedByToken(queue, userId, token)
                .filter(allowed -> allowed)
                .flatMap(allowed -> Mono.just(Response.success(new WaitingResponse(queue, userId, -1L))))
                .switchIfEmpty(userQueueService.registerWaitQueue(queue, userId)
                        .onErrorResume(ex -> userQueueService.getRank(queue, userId))
                        .map(rank -> Response.success(new WaitingResponse(queue, userId, rank)))
                );
    }

}
