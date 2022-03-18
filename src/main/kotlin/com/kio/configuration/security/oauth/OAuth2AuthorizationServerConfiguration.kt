@file:Suppress("DEPRECATION")

package com.kio.configuration.security.oauth

import com.kio.configuration.properties.OAuthConfigurationProperties
import com.kio.configuration.security.SecurityUserDetailsService
import com.kio.shared.enums.OAuthGrantType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer
import org.springframework.security.oauth2.provider.approval.JdbcApprovalStore
import org.springframework.security.oauth2.provider.token.TokenStore
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import javax.sql.DataSource

@Configuration
@EnableAuthorizationServer
class OAuth2AuthorizationServerConfiguration(
    private val authenticationManager: AuthenticationManager,
    private val passwordEncoder: PasswordEncoder,
    private val oAuthConfigurationProperties: OAuthConfigurationProperties,
    private val datasource: DataSource
) : AuthorizationServerConfigurerAdapter() {

    @Bean
    fun tokenStore(): TokenStore{
        return InMemoryTokenStore()
    }

    override fun configure(security: AuthorizationServerSecurityConfigurer) {
        security.passwordEncoder(passwordEncoder)
            .checkTokenAccess("permitAll()")
    }

    override fun configure(clients: ClientDetailsServiceConfigurer) {
        clients.inMemory()
            .withClient(oAuthConfigurationProperties.id)
            .secret(passwordEncoder.encode(oAuthConfigurationProperties.secret))
            .resourceIds("kio-id")
            .authorities("USER")
            .scopes("read")
            .authorizedGrantTypes(
                OAuthGrantType.REFRESH_TOKEN.grant,
                OAuthGrantType.PASSWORD.grant,
                OAuthGrantType.AUTHORIZATION_CODE.grant,
                OAuthGrantType.IMPLICIT.grant
            )
            .autoApprove(true)
            .accessTokenValiditySeconds(oAuthConfigurationProperties.accessTokenValidityTime)
            .refreshTokenValiditySeconds(oAuthConfigurationProperties.refreshTokenValidityTime)
            .redirectUris("http://localhost:8080/user")
    }

    override fun configure(endpoints: AuthorizationServerEndpointsConfigurer) {
        endpoints.authenticationManager(authenticationManager)
            //.approvalStore(JdbcApprovalStore(datasource))
            .tokenStore(tokenStore())
    }

}