package com.kio.controllers

import com.kio.dto.response.UserDTO
import com.kio.dto.request.SignUpRequest
import com.kio.services.UserService
import com.kio.shared.utils.SecurityUtil
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService,
) {

    @PostMapping
    fun createNewUserAccount(@RequestBody @Valid request: SignUpRequest): ResponseEntity<UserDTO>{
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(userService.save(request))
    }

    @GetMapping
    fun findByUsernameOrEmail(@RequestParam(name = "q") query: String): ResponseEntity<UserDTO> {
        return ResponseEntity.status(HttpStatus.OK)
            .body(userService.findByUsernameOrEmail(query))
    }

    @GetMapping(path = ["/me"])
    fun me(principal: Principal): ResponseEntity<UserDTO> {
        val currentUser = SecurityUtil.getAuthenticatedUser()

        val dto = UserDTO(
            id = currentUser.id!!,
            username = currentUser.username,
            email = currentUser.email,
            profilePictureId = currentUser.profilePictureBucketKey
        )

        return ResponseEntity.status(HttpStatus.OK)
            .body(dto)
    }

}