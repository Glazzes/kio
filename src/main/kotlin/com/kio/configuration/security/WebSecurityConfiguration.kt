package com.kio.configuration.security

import com.kio.repositories.UserRepository
import com.kio.shared.exception.NotFoundException
import org.springframework.context.annotation.Bean
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import javax.annotation.PostConstruct

@EnableWebSecurity
class WebSecurityConfiguration(private val userRepository: UserRepository) {

    @PostConstruct
    fun setContextHolderStrategy() {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL)
    }

    @Bean
    fun defaultSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http.authorizeHttpRequests {
            it.anyRequest().permitAll()
        }
            .csrf { it.disable() }
            .cors {
                it.configurationSource {
                    val configuration = CorsConfiguration()
                    configuration.allowCredentials = true
                    configuration.allowedOrigins = listOf("http://localhost:19006")
                    configuration.allowedMethods = listOf("GET", "POST", "PATCH", "DELETE", "OPTIONS")
                    configuration.maxAge = 3600
                    configuration
                }
            }
            .httpBasic { it.realmName("Kio realm") }
            .oauth2ResourceServer {
                it.jwt { c -> c.jwkSetUri("http://localhost:8080/oauth2/jwks")
                    .jwtAuthenticationConverter(this.jwtToUserConverter())
                }
            }
            .formLogin(Customizer.withDefaults())
            .build()
    }

    private fun jwtToUserConverter() = Converter<Jwt, AbstractAuthenticationToken> {
        val authenticatedUser = userRepository.findByUsername(it.subject)
            ?: throw NotFoundException("Could not found user with username ${it.subject}")

        UsernamePasswordAuthenticationToken(authenticatedUser, null)
    }

    @Bean
    fun userDetailsService() = UserDetailsService {
        val authenticatedUser = userRepository.findByUsername(it) ?:
            throw UsernameNotFoundException("Could not found user with username $it")

        UserToUserDetailsAdapter(authenticatedUser)
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}