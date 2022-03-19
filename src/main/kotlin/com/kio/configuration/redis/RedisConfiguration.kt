package com.kio.configuration.redis

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate

@Configuration
class RedisConfiguration {

    @Bean
    fun redisTemplate(): RedisTemplate<String, Boolean> {
        val redisTemplate = RedisTemplate<String, Boolean>()
        redisTemplate.setConnectionFactory(redisConnectionFactory())
        return redisTemplate
    }

    @Bean
    fun redisConnectionFactory(): LettuceConnectionFactory {
        return LettuceConnectionFactory("localhost", 6379)
    }

}