package com.kio.configuration.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "kio.oauth.client")
data class OAuth2ConfigurationProperties(
    val id: String,
    val secret: String,
    val refreshTokenValidityTime: Int,
    val accessTokenValidityTime: Int,
)
