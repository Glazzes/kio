package com.kio.configuration.security

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.web.SecurityFilterChain
import javax.annotation.PostConstruct

@EnableWebSecurity
class WebSecurityConfiguration(
    private val userDetailsService: UserDetailsService,
    private val jwtToUserConverter: JwtToUserConverter
) {

    @PostConstruct
    fun setContextHolderStrategy() {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL)
    }

    @Bean
    fun authenticationManager(): AuthenticationManager {
        val daoProvider = DaoAuthenticationProvider()
        daoProvider.setPasswordEncoder(this.passwordEncoder())
        daoProvider.setUserDetailsService(userDetailsService)

        return ProviderManager(daoProvider)
    }

    @Bean
    fun defaultSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http.csrf { it.disable() }
            .httpBasic { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.antMatchers("/api/v1/users/me", "/api/v1/auth/revoke").authenticated()
                    .antMatchers("/api/v1/auth/**", "/api/v1/users").permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer {
                it.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtToUserConverter)
                }
            }
            .build()
    }

    @Bean
    fun jwkSource(): JWKSource<SecurityContext?> {
        val rsaKey: RSAKey = KeyUtils.getRsaKey()
        val jwkSet = JWKSet(rsaKey)
        return ImmutableJWKSet<SecurityContext>(jwkSet)
    }

    @Bean
    @Primary
    fun customJwtDecoder(): JwtDecoder {
        val rsaKey = KeyUtils.getRsaKey()
        return NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey())
            .build()
    }

    @Bean
    @Primary
    fun customJwtEncoder(): JwtEncoder {
        val rsaKey = KeyUtils.getRsaKey()
        val jwkSource = ImmutableJWKSet<SecurityContext>(JWKSet(rsaKey))
        return NimbusJwtEncoder(jwkSource)
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}