package com.kio.configuration.security

import com.amazonaws.services.kms.model.NotFoundException
import com.kio.repositories.UserRepository
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component

@Component
class JwtToUserConverter(
    private val userRepository: UserRepository
): Converter<Jwt, AbstractAuthenticationToken> {

    override fun convert(source: Jwt): AbstractAuthenticationToken? {
        val user = userRepository.findByUsername(source.subject)
            ?: throw NotFoundException("User with username ${source.subject} was not found in jwt converter")

        return UsernamePasswordAuthenticationToken(user, user.password, emptyList())
    }

}