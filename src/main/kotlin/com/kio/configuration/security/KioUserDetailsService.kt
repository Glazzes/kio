package com.kio.configuration.security

import com.kio.services.UserService
import com.kio.shared.exception.NotFoundException
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component
import java.lang.IllegalArgumentException

// @Component
class KioUserDetailsService(val userService: UserService) : UserDetailsService {
    override fun loadUserByUsername(username: String?): UserDetails {
        val name = username ?: throw IllegalArgumentException("Username must not be null")

        return userService.findByUsername(name)
            .map {
                val currentUsername = it.username ?: throw IllegalStateException("username must not be null")
                val password = it.password ?: throw IllegalStateException("password must not be null")

                KioUserDetails(
                    currentUsername,
                    password,
                    mutableListOf(SimpleGrantedAuthority("USER"))
                )
            }
            .orElseThrow {NotFoundException("Could not find user with username $username")}
    }
}