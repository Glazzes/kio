@file:Suppress("DEPRECATION")

package com.kio.configuration.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer
import org.springframework.security.oauth2.provider.token.TokenStore
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore

@Configuration
@EnableAuthorizationServer
class OAuthServerConfiguration(val authenticationManager: AuthenticationManager) :AuthorizationServerConfigurerAdapter() {

    @Bean
    fun tokenStore(): TokenStore{
        return InMemoryTokenStore()
    }

    override fun configure(clients: ClientDetailsServiceConfigurer) {
        clients.inMemory()
            .withClient("client_id")
            .secret("client_secret")
            .authorities("USER")
            .resourceIds("my_resource_id")
            .scopes("read")
            .authorizedGrantTypes("authorization_code", "password")
            .redirectUris("http://localhost:8080/")
    }

    override fun configure(endpoints: AuthorizationServerEndpointsConfigurer) {
        endpoints.authenticationManager(authenticationManager)
            .tokenStore(tokenStore())
    }
}