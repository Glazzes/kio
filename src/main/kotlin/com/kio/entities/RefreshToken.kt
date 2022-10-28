package com.kio.entities

import java.time.LocalDateTime

class RefreshToken(
    val token: String,
    val subject: String,
    val issuedAt: LocalDateTime,
) : java.io.Serializable