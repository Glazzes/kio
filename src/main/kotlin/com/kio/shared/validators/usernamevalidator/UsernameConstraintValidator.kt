package com.kio.shared.validators.usernamevalidator

import com.kio.services.UserService
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class UsernameConstraintValidator(private val userService: UserService) :
    ConstraintValidator<UsernameMustNotBeRegistered, String> {

    override fun isValid(value: String, context: ConstraintValidatorContext?): Boolean {
        return !userService.existsByUsername(value)
    }
}