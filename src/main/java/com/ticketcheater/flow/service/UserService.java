package com.ticketcheater.flow.service;

import com.ticketcheater.flow.dto.UserDTO;
import com.ticketcheater.flow.repository.UserCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserCacheRepository userCacheRepository;

    public Mono<UserDTO> loadUserByUsername(String username) {
        return userCacheRepository.getUser(username);
    }

}
