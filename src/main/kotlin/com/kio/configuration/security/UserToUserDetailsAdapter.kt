package com.kio.configuration.security

import com.kio.entities.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserToUserDetailsAdapter(val user: User) : UserDetails {

    fun getId(): String? {
        return user.id
    }

    override fun getUsername(): String {
        return user.username
    }

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
       return mutableListOf<GrantedAuthority>(SimpleGrantedAuthority("user"))
    }

    override fun getPassword(): String {
        return user.password
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