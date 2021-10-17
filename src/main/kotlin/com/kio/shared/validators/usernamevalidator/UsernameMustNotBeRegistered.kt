package com.kio.shared.validators.usernamevalidator

import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

@Retention(value = AnnotationRetention.RUNTIME)
@Target(allowedTargets = [AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY_GETTER])
@Constraint(validatedBy = [UsernameConstraintValidator::class])
annotation class UsernameMustNotBeRegistered(
    val message: String = "This username has been taken by another user",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
