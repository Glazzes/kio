package com.kio.repositories

import com.kio.entities.Notification
import com.kio.entities.User
import org.springframework.data.mongodb.repository.MongoRepository

interface NotificationRepository : MongoRepository<Notification, Long> {
    // fun findByReceiver(user: User): List<Notification>
}