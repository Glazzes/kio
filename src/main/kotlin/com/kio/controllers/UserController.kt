package com.kio.controllers

import com.kio.entities.models.SignUpRequest
import com.kio.services.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/user")
class UserController(private val userService: UserService){

    @GetMapping
    fun dummy(): String{
        return "Hello world"
    }

    @PostMapping
    fun createNewUserAccount(
        @RequestBody @Valid request: SignUpRequest,
        bindingResult: BindingResult
    ): ResponseEntity<*>{
        if(bindingResult.hasFieldErrors()){
            val fieldErrors: MutableMap<String, String?> = HashMap()
            for(error in bindingResult.fieldErrors){
                fieldErrors[error.field] = error.defaultMessage
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(fieldErrors)
        }

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(userService.save(request))
    }

}