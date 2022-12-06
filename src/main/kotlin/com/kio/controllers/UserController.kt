package com.kio.controllers

import com.kio.dto.request.EditUserRequest
import com.kio.dto.response.UserDTO
import com.kio.dto.request.SignUpRequest
import com.kio.dto.response.ExistsResponseDTO
import com.kio.services.UserService
import com.kio.shared.utils.SecurityUtil
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.security.Principal
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService,
) {

    @PostMapping
    fun createNewUserAccount(@RequestBody @Valid request: SignUpRequest): ResponseEntity<Unit> {
        userService.save(request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .build()
    }

    @GetMapping
    fun findByUsernameOrEmail(@RequestParam(name = "q") query: String): ResponseEntity<UserDTO> {
        return ResponseEntity.status(HttpStatus.OK)
            .body(userService.findByUsernameOrEmail(query))
    }

    @GetMapping("/exists")
    fun existsByUsernameOrEmail(@RequestParam username: String?, @RequestParam email: String?): ResponseEntity<ExistsResponseDTO> {
        return ResponseEntity.status(HttpStatus.OK)
            .body(userService.existsByUsernameOrEmail(username, email))
    }

    @PatchMapping(path = ["/edit"])
    fun edit(@RequestParam request: EditUserRequest, @RequestParam file: MultipartFile?): ResponseEntity<UserDTO> {
        return ResponseEntity.status(HttpStatus.OK)
            .body(userService.edit(request, file))
    }

    @GetMapping(path = ["/me"])
    fun me(principal: Principal): ResponseEntity<UserDTO> {
        val currentUser = SecurityUtil.getAuthenticatedUser()

        val dto = UserDTO(
            id = currentUser.id!!,
            username = currentUser.username,
            email = currentUser.email,
            pictureId = currentUser.profilePictureId
        )

        return ResponseEntity.status(HttpStatus.OK)
            .body(dto)
    }

}