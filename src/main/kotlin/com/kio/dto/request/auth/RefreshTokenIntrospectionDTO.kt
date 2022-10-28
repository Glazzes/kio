package com.kio.dto.request.auth

import java.time.LocalDateTime

data class RefreshTokenIntrospectionDTO(
    val token: String,
    val subject: String,
    val issuedAt: LocalDateTime
)
