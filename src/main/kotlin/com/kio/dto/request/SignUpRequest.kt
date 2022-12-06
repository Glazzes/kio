package com.kio.dto.request

import com.kio.shared.validators.emailvalidator.EmailMustNotBeRegistered
import com.kio.shared.validators.usernamevalidator.UsernameMustNotBeRegistered
import org.hibernate.validator.constraints.Length
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern

data class SignUpRequest(

    @get:UsernameMustNotBeRegistered
    @get:NotBlank(message = " * Username is required")
    @get:Length(min = 3, max = 50, message = "* Username must be between 3 and 50 characters long")
    val username: String,

    @get:EmailMustNotBeRegistered
    @get:NotBlank(message = "* Email is required")
    @get:Email(message = "* Invalid email i.e kiouser@gmail.com")
    val email: String,

    @get:NotBlank(message = "* A password is required")
    @get:Length(min = 8, max = 100, message = "* Password must be at least 8 characters long and less than 100")
    @get:Pattern(regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&-+=()])(?=\\\\S+\$).{8,20}\$", message = "Password must contain at least one uppercase letter and one digit")
    val password: String
)