package com.kio.shared.utils

import com.kio.entities.User
import org.springframework.security.core.context.SecurityContextHolder

object SecurityUtil {

    fun getAuthenticatedUser(): User {
        return SecurityContextHolder.getContext()
            .authentication
            .principal as User
    }

}