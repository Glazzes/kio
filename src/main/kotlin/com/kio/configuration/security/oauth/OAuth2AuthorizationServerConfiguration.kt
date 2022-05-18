@file:Suppress("DEPRECATION")

package com.kio.configuration.security.oauth

import com.kio.configuration.properties.OAuthConfigurationProperties
import com.kio.configuration.security.SecurityUserDetailsService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer
import org.springframework.security.oauth2.provider.token.TokenStore
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore

@Configuration
@EnableAuthorizationServer
class OAuth2AuthorizationServerConfiguration(
    private val authenticationManager: AuthenticationManager,
    private val userDetailsService: SecurityUserDetailsService,
    private val passwordEncoder: PasswordEncoder,
    private val oAuthConfigurationProperties: OAuthConfigurationProperties,
) : AuthorizationServerConfigurerAdapter() {

    @Bean
    fun tokenStore(): TokenStore{
        return InMemoryTokenStore()
    }


    fun tokenConverter(): JwtAccessTokenConverter {
        val converter = JwtAccessTokenConverter()
        converter.setSigningKey("secret")
        return converter
    }

    override fun configure(security: AuthorizationServerSecurityConfigurer) {
        security
            .passwordEncoder(passwordEncoder)
            .checkTokenAccess("permitAll()")
    }

    override fun configure(clients: ClientDetailsServiceConfigurer) {
        clients.inMemory()
            .withClient(oAuthConfigurationProperties.id)
            .secret(passwordEncoder.encode(oAuthConfigurationProperties.secret))
            .resourceIds("kio-id")
            .authorities("USER")
            .scopes("read")
            .authorizedGrantTypes("refresh_token", "authorization_code", "password")
            .autoApprove(true)
            .accessTokenValiditySeconds(oAuthConfigurationProperties.accessTokenValidityTime)
            .refreshTokenValiditySeconds(oAuthConfigurationProperties.refreshTokenValidityTime)
            .redirectUris("http://localhost:8080/user")
    }

    override fun configure(endpoints: AuthorizationServerEndpointsConfigurer) {
        endpoints.authenticationManager(authenticationManager)
            .tokenStore(tokenStore())
            .userDetailsService(userDetailsService) // allows refresh_token to work
            // .accessTokenConverter(this.tokenConverter())
    }

}