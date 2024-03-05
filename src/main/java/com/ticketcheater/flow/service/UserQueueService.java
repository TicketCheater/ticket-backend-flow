package com.ticketcheater.flow.service;

import com.ticketcheater.flow.exception.ErrorCode;
import com.ticketcheater.flow.exception.TicketApplicationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserQueueService {

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    private static final String USER_QUEUE_WAIT_KEY = "users:queue:%s:wait";
    private static final String USER_QUEUE_PROCEED_KEY = "users:queue:%s:proceed";
    private static final Long ALLOW_COUNT = 100L;

    public Mono<Long> registerWaitQueue(String queue, Long userId) {
        long unixTimestamp = Instant.now().getEpochSecond();
        return reactiveRedisTemplate.opsForZSet().add(USER_QUEUE_WAIT_KEY.formatted(queue), userId.toString(), unixTimestamp)
                .filter(i -> i)
                .switchIfEmpty(Mono.error(() -> new TicketApplicationException(ErrorCode.QUEUE_ALREADY_REGISTERED_USER, String.format("%d is already registered", userId))))
                .flatMap(i -> reactiveRedisTemplate.opsForZSet().rank(USER_QUEUE_WAIT_KEY.formatted(queue), userId.toString()))
                .map(i -> i>=0 ? i+1: i);
    }

    public Mono<Long> allowUser(String queue) {
        return reactiveRedisTemplate.opsForZSet().popMin(USER_QUEUE_WAIT_KEY.formatted(queue), ALLOW_COUNT)
                .flatMap(member -> reactiveRedisTemplate.opsForZSet().add(USER_QUEUE_PROCEED_KEY.formatted(queue), member.getValue(), Instant.now().getEpochSecond()))
                .count();
    }

    public Mono<Long> getRank(String queue, Long userId) {
        return reactiveRedisTemplate.opsForZSet().rank(USER_QUEUE_WAIT_KEY.formatted(queue), userId.toString())
                .defaultIfEmpty(-1L)
                .map(rank -> rank>=0 ? rank+1: rank);
    }

    public Mono<String> generateToken(String queue, Long userId) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            String input = "user-queue-%s-%d".formatted(queue, userId);
            byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();

            for (byte aByte : encodedHash) hexString.append(String.format("%02x", aByte));

            return Mono.just(hexString.toString());

        } catch (NoSuchAlgorithmException e) {
            throw new TicketApplicationException(ErrorCode.NO_SUCH_ALGORITHM, "");
        }
    }

    public Mono<Boolean> isAllowedByToken(String queue, Long userId, String token) {
        return this.generateToken(queue, userId)
                .filter(gen -> gen.equalsIgnoreCase(token))
                .map(i -> true)
                .defaultIfEmpty(false);
    }

}
