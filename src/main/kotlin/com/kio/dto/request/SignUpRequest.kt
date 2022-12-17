package com.kio.dto.request

import com.kio.shared.validators.emailvalidator.EmailMustNotBeRegistered
import com.kio.shared.validators.usernamevalidator.UsernameMustNotBeRegistered
import org.hibernate.validator.constraints.Length
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern

data class SignUpRequest(

    @get:UsernameMustNotBeRegistered(message = "{constraints.username.registered}")
    @get:NotBlank(message = "{constraints.username.required}")
    @get:Length(min = 3, max = 50, message = "{constraints.username.length}")
    val username: String,

    @get:EmailMustNotBeRegistered(message = "{constraints.password.registered}")
    @get:NotBlank(message = "{constraints.email.required}")
    @get:Email(message = "{constraints.email.invalid}")
    val email: String,

    @get:NotBlank(message = "{constraints.password.required}")
    @get:Length(min = 8, max = 100, message = "{constraints.password.length}")
    // @get:Pattern(regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&-+=()])(?=\\\\S+\$).{8,20}\$", message = "Password must contain at least one uppercase letter and one digit")
    val password: String
)