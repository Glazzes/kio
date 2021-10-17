package com.kio.shared.validators.emailvalidator

import com.kio.services.UserService
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class EmailConstraintValidator(private val userService: UserService) :
    ConstraintValidator<EmailMustNotBeRegistered, String> {

    override fun isValid(value: String, context: ConstraintValidatorContext?): Boolean {
        return !userService.existsByEmail(value)
    }

}