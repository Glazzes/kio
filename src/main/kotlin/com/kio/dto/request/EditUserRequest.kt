package com.kio.dto.request

import org.hibernate.validator.constraints.Length
import javax.validation.constraints.Email

data class EditUserRequest(
    @get:Length(min = 3, max = 50, message = "{constraints.username.length}")
    val username: String,

    @get:Email(message = "{constraints.email.invalid}")
    val email: String,
    val password: String?
)
