package com.kio.dto.request

import com.kio.shared.validators.emailvalidator.EmailMustNotBeRegistered
import com.kio.shared.validators.usernamevalidator.UsernameMustNotBeRegistered
import org.hibernate.validator.constraints.Length
import javax.validation.constraints.Email

data class SignUpRequest(

    @get:UsernameMustNotBeRegistered
    @get:Length(min = 3, max = 50, message = "Username must be at least 3 characters long and less than 50")
    val username: String,

    @get:EmailMustNotBeRegistered
    @get:Email(message = "Invalid email i.e kiouser@gmail.com")
    val email: String,

    @get:Length(min = 8, max = 100, message = "Password must be at least 8 characters long and less than 100")
    val password: String

)