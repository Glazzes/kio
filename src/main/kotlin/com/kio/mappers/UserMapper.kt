package com.kio.mappers

import com.kio.dto.response.ContributorDTO
import com.kio.dto.response.UserDTO
import com.kio.entities.User

object UserMapper {

    fun toUserDTO(user: User) = UserDTO(
        id = user.id!!,
        username = user.username,
        email = user.email,
        profilePictureId = ""
    )

    fun toContributorDTO(user: User) = ContributorDTO(
        id = user.id!!,
        username = user.username,
        profilePictureId = ""
    )

}