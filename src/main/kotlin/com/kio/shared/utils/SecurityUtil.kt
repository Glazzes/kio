package com.kio.shared.utils

import com.kio.configuration.security.UserToUserDetailsAdapter
import com.kio.entities.mongo.User
import org.springframework.security.core.context.SecurityContextHolder

object SecurityUtil {

    fun getAuthenticatedUser(): User {
        val authenticatedUser = SecurityContextHolder.getContext()
            .authentication
            .principal as UserToUserDetailsAdapter

        return authenticatedUser.user
    }

}