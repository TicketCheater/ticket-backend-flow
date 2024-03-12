package com.ticketcheater.flow.repository;

import com.ticketcheater.flow.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Log4j2
@Repository
@RequiredArgsConstructor
public class UserCacheRepository {

    private final ReactiveRedisTemplate<String, UserDTO> reactiveAuthRedisTemplate;

    public Mono<UserDTO> getUser(String username) {
        return reactiveAuthRedisTemplate.opsForValue().get(getKey(username))
                .doOnNext(data -> log.info("Get User from Redis {}", data));
    }

    private String getKey(String userName) {
        return "USERDTO:" + userName;
    }

}
