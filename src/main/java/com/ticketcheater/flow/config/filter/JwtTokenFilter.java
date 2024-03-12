package com.ticketcheater.flow.config.filter;

import com.ticketcheater.flow.service.UserService;
import com.ticketcheater.flow.util.JwtTokenUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Log4j2
@RequiredArgsConstructor
public class JwtTokenFilter implements WebFilter {

    private final UserService userService;
    private final String secretKey;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        final String header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (header == null || !header.startsWith("Bearer ")) {
            log.warn("Authorization Header does not start with Bearer");
            return chain.filter(exchange);
        }

        final String token = header.split(" ")[1].trim();

        String username = JwtTokenUtils.getUsername(token, secretKey);
        return userService.loadUserByUsername(username)
                .flatMap(userDetails -> {
                    if (Boolean.FALSE.equals(JwtTokenUtils.validate(token, userDetails.getUsername(), secretKey))) {
                        return chain.filter(exchange);
                    }

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );

                    exchange.getAttributes().put(SecurityContext.class.getName(), authentication);

                    return chain.filter(exchange);
                });
    }

}
