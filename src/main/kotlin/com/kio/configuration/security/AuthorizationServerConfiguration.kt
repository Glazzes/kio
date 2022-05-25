package com.kio.configuration.security

import com.kio.configuration.properties.OAuth2ConfigurationProperties
import com.nimbusds.jose.jwk.JWKSelector
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.config.ClientSettings
import org.springframework.security.oauth2.server.authorization.config.ProviderSettings
import org.springframework.security.oauth2.server.authorization.config.TokenSettings
import org.springframework.security.web.SecurityFilterChain
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.Duration
import java.util.*


@Configuration
class AuthorizationServerConfiguration(private val oauthProperties: OAuth2ConfigurationProperties) {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    fun oauth2SecurityFilterChain(httpSecurity: HttpSecurity): SecurityFilterChain {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(httpSecurity)

        return httpSecurity.formLogin(Customizer.withDefaults())
            .build()
    }

    @Bean
    fun registedClientRepository(): RegisteredClientRepository {
        val kio = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId(oauthProperties.id)
            .clientSecret(oauthProperties.secret)
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .redirectUri("http://localhost:8080/authorized")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .tokenSettings(
                TokenSettings.builder()
                    .accessTokenTimeToLive(Duration.ofMinutes(15))
                    .refreshTokenTimeToLive(Duration.ofDays(7))
                    .reuseRefreshTokens(false)
                    .build()
            )
            .clientSettings(
                ClientSettings.builder()
                    .requireAuthorizationConsent(false)
                    .build()
            )
            .scope("read")
            .build()

        return InMemoryRegisteredClientRepository(kio)
    }

    @Bean
    fun providerSettings(): ProviderSettings {
        return ProviderSettings.builder()
            .issuer("http://localhost:8080")
            .build()
    }

    @Bean
    fun jwkSource(): JWKSource<SecurityContext?> {
        val rsaKey: RSAKey = generateRsa()
        val jwkSet = JWKSet(rsaKey)
        return JWKSource<SecurityContext?> { jwkSelector: JWKSelector, securityContext: SecurityContext? ->
            jwkSelector.select(
                jwkSet
            )
        }
    }

    private fun generateRsa(): RSAKey {
        val keyPair: KeyPair = generateRsaKey()
        val publicKey: RSAPublicKey = keyPair.public as RSAPublicKey
        val privateKey: RSAPrivateKey = keyPair.private as RSAPrivateKey
        return RSAKey.Builder(publicKey)
            .privateKey(privateKey)
            .keyID(UUID.randomUUID().toString())
            .build()
    }

    private fun generateRsaKey(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        return keyPairGenerator.generateKeyPair()
    }

}