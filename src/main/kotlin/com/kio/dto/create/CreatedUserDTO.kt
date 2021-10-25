package com.kio.dto.create

data class CreatedUserDTO(
    val username: String,
    val nickname: String,
    val spaceUsed: Long,
    val profilePicture: String
)