package com.ticketcheater.flow.config;

import com.ticketcheater.flow.dto.UserDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.flow.port}")
    private int flowPort;

    @Value("${spring.data.redis.auth.port}")
    private int authPort;

    @Bean
    @Primary
    public ReactiveRedisConnectionFactory reactiveFlowRedisConnectionFactory() {
        return new LettuceConnectionFactory(host, flowPort);
    }

    @Bean
    public ReactiveRedisTemplate<String, String> reactiveFlowRedisTemplate(ReactiveRedisConnectionFactory reactiveFlowRedisConnectionFactory) {
        RedisSerializationContext<String, String> serializationContext = RedisSerializationContext
                .<String, String>newSerializationContext()
                .key(new StringRedisSerializer())
                .value(new StringRedisSerializer())
                .hashKey(new StringRedisSerializer())
                .hashValue(new StringRedisSerializer())
                .build();

        return new ReactiveRedisTemplate<>(reactiveFlowRedisConnectionFactory, serializationContext);
    }

    @Bean("next")
    public ReactiveRedisConnectionFactory reactiveAuthRedisConnectionFactory() {
        return new LettuceConnectionFactory(host, authPort);
    }

    @Bean
    public ReactiveRedisTemplate<String, UserDTO> reactiveAuthRedisTemplate(@Qualifier("next") ReactiveRedisConnectionFactory reactiveAuthRedisConnectionFactory) {
        RedisSerializationContext<String, UserDTO> serializationContext = RedisSerializationContext
                .<String, UserDTO>newSerializationContext()
                .key(new StringRedisSerializer())
                .value(new Jackson2JsonRedisSerializer<>(UserDTO.class))
                .hashKey(new StringRedisSerializer())
                .hashValue(new Jackson2JsonRedisSerializer<>(UserDTO.class))
                .build();

        return new ReactiveRedisTemplate<>(reactiveAuthRedisConnectionFactory, serializationContext);
    }

}
