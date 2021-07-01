package com.kio.repositories

import com.kio.entities.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, String> {
    fun existsByEmail(email: String): Boolean
    fun existsByUsername(username: String): Boolean
}