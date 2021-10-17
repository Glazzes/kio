package com.kio.configuration.security

import com.kio.entities.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.lang.IllegalArgumentException

class SecurityUserAdapter(val user: User) : UserDetails {

    override fun getUsername(): String {
        return user.username ?: throw IllegalArgumentException("Username must not be null")
    }

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
       return mutableListOf<GrantedAuthority>(SimpleGrantedAuthority("user"))
    }

    override fun getPassword(): String {
        return user.password ?: throw IllegalArgumentException("Username must not be null")
    }

    fun getAuthenticatedUser(): User{
        return user
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }
}