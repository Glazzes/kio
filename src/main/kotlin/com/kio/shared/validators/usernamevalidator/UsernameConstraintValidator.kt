package com.kio.shared.validators.usernamevalidator

import com.kio.repositories.UserRepository
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class UsernameConstraintValidator(private val userService: UserRepository) :
    ConstraintValidator<UsernameMustNotBeRegistered, String> {

    override fun isValid(value: String, context: ConstraintValidatorContext?): Boolean {
        return !userService.existsByUsername(value)
    }
}