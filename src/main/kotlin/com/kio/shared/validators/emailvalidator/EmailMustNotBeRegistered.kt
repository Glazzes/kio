package com.kio.shared.validators.emailvalidator

import javax.validation.Constraint

@Retention(value = AnnotationRetention.RUNTIME)
@Target(allowedTargets = [AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY_GETTER])
@Constraint(validatedBy = [EmailConstraintValidator::class])
annotation class EmailMustNotBeRegistered(
    val message: String = "There's an account already with this username",
)
