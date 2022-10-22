package com.kio.services

import com.kio.shared.enums.JwtType
import com.kio.dto.request.auth.LoginDTO
import com.kio.dto.request.auth.TokenResponseDTO
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class AuthenticationService (
    private val jwtEncoder: JwtEncoder,
    private val jwtDecoder: JwtDecoder,
) {

    private val isRefreshToken = "isRefreshToken"

    fun login(loginDTO: LoginDTO): TokenResponseDTO {
        val accessToken = this.issueToken(loginDTO.username, JwtType.ACCESS_TOKEN)
        val refreshToken = this.issueToken(loginDTO.username, JwtType.REFRESH_TOKEN)

        return TokenResponseDTO(accessToken, refreshToken)
    }

    fun getTokenPair(refreshToken: String): TokenResponseDTO {
        val jwt = jwtDecoder.decode(refreshToken)
        val now = Instant.now()
        val expiration = jwt.expiresAt!!

        if(!jwt.getClaimAsBoolean(isRefreshToken) && expiration.isBefore(now)) {
            throw RuntimeException("Random message :(")
        }

        val newRefreshToken = this.issueToken(jwt.subject, JwtType.ACCESS_TOKEN)
        val accessToken = this.issueToken(jwt.subject, JwtType.REFRESH_TOKEN)
        return TokenResponseDTO(accessToken, newRefreshToken)
    }

    private fun issueToken(subject: String, type: JwtType) : String {
        val now = Instant.now()
        val duration = if(type == JwtType.ACCESS_TOKEN) 20L else 7L
        val timeUnit = if(type == JwtType.ACCESS_TOKEN) ChronoUnit.MINUTES else ChronoUnit.DAYS

        val claimsSet = JwtClaimsSet.builder()
            .issuer("kio")
            .subject(subject)
            .expiresAt(now.plus(duration, timeUnit))
            .issuedAt(now)

        if(type == JwtType.REFRESH_TOKEN) {
            claimsSet.claim(isRefreshToken, true)
        }

        val claims = claimsSet.build()
        val params = JwtEncoderParameters.from(claims)
        return jwtEncoder.encode(params).tokenValue
    }

}