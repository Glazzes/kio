package com.kio.entities.models

import org.hibernate.validator.constraints.Length
import javax.validation.constraints.Email

data class SignUpRequest(

    @Length(min = 3, max = 50, message = "Username must be at least 3 characters long and less than 50")
    val username: String,

    @Email(message = "Invalid email i.e connectuser@gmail.com")
    val email: String,

    @Length(min = 8, max = 100, message = "Password must be at least 8 characters long and less than 100")
    val password: String
)