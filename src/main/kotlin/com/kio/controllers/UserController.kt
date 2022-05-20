package com.kio.controllers

import com.kio.dto.response.UserDTO
import com.kio.dto.request.SignUpRequest
import com.kio.services.UserService
import com.kio.shared.utils.ControllerUtil
import com.kio.shared.utils.SecurityUtil
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/users")
class UserController(private val userService: UserService) {

    @PostMapping
    fun createNewUserAccount(
        @RequestBody @Valid request: SignUpRequest,
        bindingResult: BindingResult
    ): ResponseEntity<*>{
        if(bindingResult.hasFieldErrors()){
            val fieldErrors = ControllerUtil.getRequestErrors(bindingResult)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(fieldErrors)
        }

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(userService.save(request))
    }

    @GetMapping(path = ["/me"])
    fun me(): ResponseEntity<UserDTO> {
        val currentUser = SecurityUtil.getAuthenticatedUser()

        val dto = UserDTO(
            id = currentUser.id!!,
            username = currentUser.username,
            email = currentUser.email,
            profilePictureId = ""
        )

        return ResponseEntity.status(HttpStatus.OK)
            .body(dto)
    }

}