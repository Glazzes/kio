package com.kio.repositories

import com.kio.entities.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.mongodb.repository.MongoRepository

interface UserRepository : MongoRepository<User, String> {
    fun findByUsername(username: String): User?
    fun existsByEmail(email: String): Boolean
    fun existsByUsername(username: String): Boolean
    fun findByIdIn(ids: Collection<String>): Collection<User>
    fun findByIdIn(ids: Collection<String>, pageRequest: PageRequest): Page<User>
    fun findByUsernameOrEmail(username: String, email: String): User?
}