package com.kio.dto.request.auth

data class TokenResponseDTO(
    val accessToken: String,
    val refreshToken: String
)