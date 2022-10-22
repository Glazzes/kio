package com.kio.configuration.security

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.kio.dto.request.auth.LoginDTO
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.xml.bind.DataBindingException

class CustomAuthenticationFilter(authenticationManager: AuthenticationManager)
    : UsernamePasswordAuthenticationFilter(authenticationManager) {

    init {
        this.setFilterProcessesUrl("/api/v1/auth/login")
    }

    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse?): Authentication {
        try{
            val loginDto = jacksonObjectMapper().readValue(request.inputStream, LoginDTO::class.java)
            val authentication = UsernamePasswordAuthenticationToken(loginDto.username, loginDto.password)

            return authenticationManager.authenticate(authentication)
        }catch (e: DataBindingException) {
            throw RuntimeException(e)
        }
    }

    override fun successfulAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
        authResult: Authentication
    ) {

        response.status = HttpStatus.OK.value()
        chain.doFilter(request, response)
    }
}