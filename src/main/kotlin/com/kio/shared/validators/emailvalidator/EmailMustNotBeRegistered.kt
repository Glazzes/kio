package com.kio.shared.validators.emailvalidator

import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

@Retention(value = AnnotationRetention.RUNTIME)
@Target(allowedTargets = [AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY_GETTER])
@Constraint(validatedBy = [EmailConstraintValidator::class])
annotation class EmailMustNotBeRegistered(
    val message: String = "There's an account already with this email",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
