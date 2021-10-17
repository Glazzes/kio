package com.kio.configuration.security

import com.kio.services.UserService
import com.kio.shared.exception.NotFoundException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component

@Component
class SecurityUserDetailsService(private val userService: UserService) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        return userService.findByUsername(username)
            .map { SecurityUserAdapter(it) }
            .orElseThrow {NotFoundException("Could not find user with username $username")}
    }
}