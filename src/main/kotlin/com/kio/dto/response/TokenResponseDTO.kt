package com.kio.dto.response

data class TokenResponseDTO(
    val accessToken: String,
    val refreshToken: String
)