package com.kio.mappers

import com.kio.dto.response.find.ContributorDTO
import com.kio.dto.response.find.UserDTO
import com.kio.entities.User

object UserMapper {

    fun toUserDTO(user: User) = UserDTO(
        id = user.id,
        username = user.username,
        email = user.email,
        profilePictureUrl = user.profilePicture.url
    )

    fun toContributorInfo(user: User) = ContributorDTO(
        id = user.id!!,
        username = user.username,
        profilePictureUrl = user.profilePicture.url
    )

}