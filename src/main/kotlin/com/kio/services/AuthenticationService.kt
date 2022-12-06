package com.kio.services

import com.kio.dto.request.auth.LoginDTO
import com.kio.dto.request.auth.TokenResponseDTO
import com.kio.entities.RefreshToken
import com.kio.repositories.UserRepository
import com.kio.shared.exception.InvalidTokenException
import com.kio.shared.exception.NotFoundException
import com.kio.shared.utils.SecurityUtil
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
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.naming.AuthenticationException

@Service
class AuthenticationService (
    private val jwtEncoder: JwtEncoder,
    private val userRepository: UserRepository,
    private val authenticationManager: AuthenticationManager,
    @Qualifier("refreshTokenTemplate") private val redisTemplate: RedisTemplate<String, RefreshToken>
) {

    fun login(loginDTO: LoginDTO): TokenResponseDTO {
        try{
            val authentication = UsernamePasswordAuthenticationToken(loginDTO.username, loginDTO.password)
            authenticationManager.authenticate(authentication)
        }catch (e: AuthenticationException) {
            throw BadCredentialsException(e.message)
        }

        val user = userRepository.findByUsername(loginDTO.username) ?:
            throw NotFoundException("No user was found with username ${loginDTO.username}")

        val accessToken = this.issueTokenAccessToken(user.id!!)
        val refreshToken = "${UUID.randomUUID()}-${UUID.randomUUID()}"

        val refreshTokenEntity = RefreshToken(refreshToken, user.id!!, LocalDateTime.now())

        redisTemplate.opsForValue()
            .set(refreshToken, refreshTokenEntity, Duration.ofDays(7L))

        return TokenResponseDTO(accessToken, refreshToken)
    }

    fun getTokenPair(refreshToken: String): TokenResponseDTO {
        val refreshTokenEntity = redisTemplate.opsForValue()
            .get(refreshToken) ?: throw InvalidTokenException("The provided refresh token has been revoked or expired")

        val accessToken = this.issueTokenAccessToken(refreshTokenEntity.subject)
        val newRefreshToken = "${UUID.randomUUID()}-${UUID.randomUUID()}"

        redisTemplate.opsForValue().getAndDelete(refreshTokenEntity.token)

        val newRefreshTokenEntity = RefreshToken(newRefreshToken, refreshTokenEntity.subject, LocalDateTime.now())
        redisTemplate.opsForValue()
            .set(newRefreshToken, newRefreshTokenEntity, Duration.ofDays(7L))

        return TokenResponseDTO(accessToken, newRefreshToken)
    }

    fun findByToken(refreshToken: String): RefreshToken {
        return redisTemplate.opsForValue().get(refreshToken) ?:
            throw NotFoundException("This token has been revoked or expired")
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

    fun revokeToken(refreshToken: String) {
        val entity = redisTemplate.opsForValue()
            .get(refreshToken) ?: throw NotFoundException("The refresh token has revoked or expired")

        val authenticatedUser = SecurityUtil.getAuthenticatedUser()
        if(authenticatedUser.username != entity.subject) {
            throw IllegalAccessException("You can not revoke a token that's not yours")
        }

        redisTemplate.opsForValue()
            .getAndDelete(refreshToken)
    }

}