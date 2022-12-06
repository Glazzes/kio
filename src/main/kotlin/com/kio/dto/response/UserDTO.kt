package com.kio.dto.response

data class UserDTO(
    val id: String,
    val username: String,
    val email: String,
    val pictureId: String?
)