package com.kio.repositories

import com.kio.entities.User
import com.kio.entities.projections.ContributorProjection
import org.springframework.data.mongodb.repository.MongoRepository

interface UserRepository : MongoRepository<User, String> {

    fun findByUsername(username: String): User?
    fun existsByEmail(email: String): Boolean
    fun existsByUsername(username: String): Boolean

    // used to get user dtos for a folder dto
    fun findByIdIn(ids: Collection<String>): Collection<ContributorProjection>
}