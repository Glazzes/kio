package com.kio.dto.response.find

data class UserDTO(
    val id: String,
    val username: String,
    val email: String,
    val profilePictureId: String?
)