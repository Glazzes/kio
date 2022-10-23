package com.kio.dto.request.auth

import org.hibernate.validator.constraints.Length

data class LoginDTO(
    @field:Length(min = 3, message = "Username must be at least 3 characters long")
    val username: String,

    @field:Length(min = 8, message = "Username must be at least 8 characters long")
    val password: String,
)