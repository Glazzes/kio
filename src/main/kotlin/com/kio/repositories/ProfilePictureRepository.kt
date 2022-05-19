package com.kio.repositories

import com.kio.entities.ProfilePicture
import org.springframework.data.mongodb.repository.MongoRepository

interface ProfilePictureRepository : MongoRepository<ProfilePicture, String> {
    fun findAllByOwnerOrderByLastTimeUsed(ownerId: String): Collection<ProfilePicture>
}