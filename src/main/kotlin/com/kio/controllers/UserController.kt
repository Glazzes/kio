package com.kio.controllers

import com.kio.entities.models.SignUpRequest
import com.kio.services.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/user")
class UserController(val userService: UserService){

    @PostMapping
    fun createNewUserAccount(@RequestBody @Valid request: SignUpRequest): ResponseEntity<Any>{
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(userService.save(request))
    }

}