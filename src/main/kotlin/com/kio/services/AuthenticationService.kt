package com.kio.services

import com.kio.dto.request.auth.LoginDTO
import com.kio.dto.request.auth.TokenResponseDTO
import com.kio.shared.exception.InvalidTokenException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.naming.AuthenticationException

@Service
class AuthenticationService (
    private val jwtEncoder: JwtEncoder,
    private val authenticationManager: AuthenticationManager,
    @Qualifier("refreshTokenTemplate") private val redisTemplate: RedisTemplate<String, String>
) {

    fun login(loginDTO: LoginDTO): TokenResponseDTO {
        try{
            val authentication = UsernamePasswordAuthenticationToken(loginDTO.username, loginDTO.password)
            authenticationManager.authenticate(authentication)
        }catch (e: AuthenticationException) {
            throw BadCredentialsException(e.message)
        }

        val accessToken = this.issueTokenAccessToken(loginDTO.username)
        val refreshToken = "${UUID.randomUUID()}-${UUID.randomUUID()}"

        redisTemplate.opsForValue()
            .set(refreshToken, loginDTO.username, Duration.ofDays(7L))

        return TokenResponseDTO(accessToken, refreshToken)
    }

    fun getTokenPair(refreshToken: String): TokenResponseDTO {
        val subject = redisTemplate.opsForValue()
            .get(refreshToken) ?: throw InvalidTokenException("The provided refresh token has been revoked or expired")

        val accessToken = this.issueTokenAccessToken(subject)
        val newRefreshToken = "${UUID.randomUUID()}-${UUID.randomUUID()}"

        redisTemplate.opsForValue()
            .getAndDelete(refreshToken)

        redisTemplate.opsForValue()
            .set(newRefreshToken, subject, Duration.ofDays(7L))

        return TokenResponseDTO(accessToken, newRefreshToken)
    }

    private fun issueTokenAccessToken(subject: String) : String {
        val now = Instant.now()

        val claimsSet = JwtClaimsSet.builder()
            .issuer("kio")
            .subject(subject)
            .expiresAt(now.plus(20L, ChronoUnit.MINUTES))
            .audience(listOf("kio-mobile-app"))
            .issuedAt(now)

        val claims = claimsSet.build()
        val params = JwtEncoderParameters.from(claims)
        return jwtEncoder.encode(params).tokenValue
    }

}