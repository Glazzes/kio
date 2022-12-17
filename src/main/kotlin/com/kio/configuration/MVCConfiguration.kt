package com.kio.configuration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.kio.dto.request.EditUserRequest
import com.kio.dto.request.file.FileUploadRequest
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.core.convert.converter.Converter
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.i18n.SessionLocaleResolver
import java.util.*

@Configuration
class MVCConfiguration {
    private val objectMapper = jacksonObjectMapper()

    @Bean
    fun messageSource(): MessageSource {
        val source = ReloadableResourceBundleMessageSource()
        source.setBasename("classpath:constraints")
        source.setDefaultEncoding("UTF-8")

        return source
    }

    @Bean
    fun validator(source: MessageSource): LocalValidatorFactoryBean {
        val validator = LocalValidatorFactoryBean()
        validator.setValidationMessageSource(source)
        return validator
    }

    @Bean
    fun localeResolver(): LocaleResolver {
        val locale = SessionLocaleResolver()
        locale.setDefaultLocale(Locale.ENGLISH)

        return locale
    }

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

    @Bean
    fun stringToSignUpRequest(): Converter<String, EditUserRequest> {
        return object : Converter<String, EditUserRequest> {
            override fun convert(source: String): EditUserRequest? {
                return objectMapper.readValue(source, EditUserRequest::class.java)
            }
        }
    }

}