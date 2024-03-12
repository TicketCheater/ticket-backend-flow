package com.ticketcheater.flow.config;

import com.ticketcheater.flow.config.filter.JwtTokenFilter;
import com.ticketcheater.flow.exception.CustomAuthenticationEntryPoint;
import com.ticketcheater.flow.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class AuthenticationConfig {

    private final UserService userService;

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(
                        exchanges -> exchanges
                                .pathMatchers("/**").authenticated()
                                .anyExchange().permitAll()
                )
                .exceptionHandling(c -> c.authenticationEntryPoint(new CustomAuthenticationEntryPoint()))
                .addFilterBefore(new JwtTokenFilter(userService, secretKey), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

}
