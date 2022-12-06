package com.kio.dto.request

import org.hibernate.validator.constraints.Length
import javax.validation.constraints.Email

data class EditUserRequest(
    @get:Length(min = 3, max = 50, message = "Username must be between 3 and 50 characters long")
    val username: String,

    @get:Email(message = "Invalid email, i.g user@kio.com")
    val email: String,
    val password: String?
)
