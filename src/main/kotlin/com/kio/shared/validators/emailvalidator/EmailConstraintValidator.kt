package com.kio.shared.validators.emailvalidator

import com.kio.services.UserService
import java.lang.IllegalArgumentException
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class EmailConstraintValidator(private val userService: UserService) :
    ConstraintValidator<EmailMustNotBeRegistered, String> {

    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        val email = value ?: throw IllegalArgumentException("Value for email constraint must no be null")
        return !userService.existsByEmail(email)
    }

}