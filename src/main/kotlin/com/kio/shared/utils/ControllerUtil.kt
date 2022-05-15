package com.kio.shared.utils

import org.springframework.validation.BindingResult

object ControllerUtil {

    fun getRequestErrors(bindingResult: BindingResult): Map<String, String> {
        val fieldErrors: MutableMap<String, String> = HashMap()
        for(error in bindingResult.fieldErrors){
            fieldErrors[error.field] = error.defaultMessage!!
        }

        return fieldErrors.toMap()
    }

}