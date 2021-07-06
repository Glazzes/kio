package com.kio.configuration.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class KioUserDetails(
    private val username: String,
    private val password: String,
    private val authorities: MutableCollection<out GrantedAuthority>
    ) : UserDetails {

    override fun getUsername(): String {
        return this.username
    }

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
       return this.authorities
    }

    override fun getPassword(): String {
        return this.password
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