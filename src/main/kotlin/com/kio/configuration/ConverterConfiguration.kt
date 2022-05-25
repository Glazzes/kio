package com.kio.configuration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.kio.dto.request.file.FileUploadRequest
import com.kio.repositories.UserRepository
import com.kio.shared.exception.NotFoundException
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.oauth2.jwt.Jwt

@Configuration
class ConverterConfiguration(private val userRepository: UserRepository) {
    private val objectMapper = jacksonObjectMapper()

    /*
     Ignore IntelliJ warnings, for some reason spring can not register this converter when expressed
     as a lambda, I'm not sure if this happens as well on Java
    */
    @Bean
    fun stringToFileUploadRequest(): Converter<String, FileUploadRequest> {
        return object : Converter<String, FileUploadRequest> {
            override fun convert(source: String): FileUploadRequest? {
                return objectMapper.readValue(source, FileUploadRequest::class.java)
            }
        }
    }

}