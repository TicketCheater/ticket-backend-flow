package com.ticketcheater.flow.controller;

import com.ticketcheater.flow.dto.response.*;
import com.ticketcheater.flow.service.UserQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
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
            Authentication authentication,
            @RequestParam(name = "token") String token
    ) {
        return userQueueService.isAllowedByToken(queue, authentication.getName(), token)
                .map(AllowedUserResponse::new)
                .map(Response::success);
    }

    @GetMapping("/rank")
    public Mono<Response<RankResponse>> getRank(
            @RequestParam(name = "queue") String queue,
            Authentication authentication
    ) {
        return userQueueService.getRank(queue, authentication.getName())
                .map(RankResponse::new)
                .map(Response::success);
    }

    @GetMapping("/touch")
    public Mono<Response<TokenResponse>> touch(
            @RequestParam(name = "queue") String queue,
            Authentication authentication,
            ServerWebExchange exchange
    ) {
        Mono<String> tokenMono = Mono.defer(() -> userQueueService.generateToken(queue, authentication.getName()));

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
            Authentication authentication,
            ServerWebExchange exchange
    ) {
        String key = "user-queue-%s-token".formatted(queue);
        HttpCookie cookieValue = exchange.getRequest().getCookies().getFirst(key);
        String token = (cookieValue == null) ? "" : cookieValue.getValue();

        return userQueueService.isAllowedByToken(queue, authentication.getName(), token)
                .filter(allowed -> allowed)
                .flatMap(allowed -> Mono.just(Response.success(new WaitingResponse(queue, -1L))))
                .switchIfEmpty(userQueueService.registerWaitQueue(queue, authentication.getName())
                        .onErrorResume(ex -> userQueueService.getRank(queue, authentication.getName()))
                        .map(rank -> Response.success(new WaitingResponse(queue, rank)))
                );
    }

}
