package com.kio.configuration.security

import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.cors.CorsConfiguration

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, jsr250Enabled = true)
class WebSecurityConfiguration : WebSecurityConfigurerAdapter() {

    override fun configure(auth: AuthenticationManagerBuilder) {
        super.configure(auth)
    }

    override fun configure(http: HttpSecurity) {
        http.csrf().disable()
            .cors().configurationSource {
                val corsConfiguration = CorsConfiguration()

                corsConfiguration.allowCredentials = true
                corsConfiguration.allowedOrigins = listOf("http://localhost:19006")
                corsConfiguration.allowedMethods = listOf("GET", "POST", "PATCH", "DELETE", "OPTIONS")
                corsConfiguration.maxAge = 3600

                corsConfiguration
            }

        http.authorizeRequests()
            .antMatchers("/user/**").permitAll()
            .anyRequest()
            .authenticated()

        http.httpBasic()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder(10)
    }

}