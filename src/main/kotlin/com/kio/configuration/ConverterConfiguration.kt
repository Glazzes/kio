package com.kio.configuration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.kio.dto.request.file.FileUploadRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter

@Configuration
class ConverterConfiguration {
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