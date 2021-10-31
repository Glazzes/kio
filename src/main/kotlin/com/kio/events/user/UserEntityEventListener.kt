package com.kio.events.user

import com.kio.entities.User
import javax.persistence.PostPersist

class UserEntityEventListener {

    @PostPersist
    fun onUserCreated(user: User){
        println("Hello world ${user.username}")
    }

}