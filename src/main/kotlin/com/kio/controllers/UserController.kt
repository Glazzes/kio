package com.kio.controllers

import com.kio.dto.response.find.UserDTO
import com.kio.dto.request.SignUpRequest
import com.kio.services.UserService
import com.kio.shared.utils.SecurityUtil
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/user")
class UserController(private val userService: UserService){

    @GetMapping
    fun dummy(auth: Authentication): String{
       val oauth = auth as OAuth2Authentication
        println(oauth.userAuthentication.toString())
        println(auth.principal::class)

        return "Hello world"
    }

    @GetMapping(path = ["/me"])
    fun me(): ResponseEntity<UserDTO> {
        val currentUser = SecurityUtil.getAuthenticatedUser()
        val dto = UserDTO(
            id = currentUser.id!!,
            username = currentUser.username,
            email = currentUser.email,
            profilePictureUrl = currentUser.profilePicture.url)

        return ResponseEntity.status(HttpStatus.OK)
            .body(dto)
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