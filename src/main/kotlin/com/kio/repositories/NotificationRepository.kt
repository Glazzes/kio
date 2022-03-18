package com.kio.repositories

import com.kio.entities.mongo.Notification
import com.kio.entities.mongo.User
import org.springframework.data.mongodb.repository.MongoRepository

interface NotificationRepository : MongoRepository<Notification, Long> {
    fun findByReceiver(user: User): List<Notification>
}