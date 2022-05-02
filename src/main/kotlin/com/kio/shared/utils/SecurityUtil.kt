package com.kio.shared.utils

import com.kio.configuration.security.UserToUserDetailsAdapter
import com.kio.entities.ProfilePicture
import com.kio.entities.UnitSummary
import com.kio.entities.User
import org.springframework.security.core.context.SecurityContextHolder

object SecurityUtil {

    fun getAuthenticatedUser(): User {
        val defaultProfilePicture = ProfilePicture(
            id = "0",
            owner = "KIO",
            isActive = true,
            url = ""
        )

        val authenticatedUser = SecurityContextHolder.getContext()
            .authentication
            .principal

        return when(authenticatedUser) {
            is UserToUserDetailsAdapter -> authenticatedUser.user
            else -> User(
                username = "anonymous",
                password = "",
                email = "",
                unitSummary = UnitSummary(),
                profilePicture = defaultProfilePicture)
        }
    }

}