package com.kio.events.user

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Paths
import javax.persistence.PostPersist
import javax.persistence.PostRemove

@Component
class UserEntityEventListener {
    @Value("\${kio.store.folder}") private lateinit var root: String

    @PostPersist
    fun onUserCreated(user: User){
        val path = Paths.get(root, user.username)
        if(!Files.exists(path)){
            Files.createDirectory(path)
        }
    }

    @PostRemove
    fun onUserDeleted(user: User){
        val path = Paths.get(root, user.id)
        Files.deleteIfExists(path)
    }

}