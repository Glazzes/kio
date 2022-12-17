package com.kio.controllers

import com.kio.dto.request.EditUserRequest
import com.kio.dto.response.UserDTO
import com.kio.dto.request.SignUpRequest
import com.kio.dto.response.ExistsResponseDTO
import com.kio.entities.enums.Plan
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

    @GetMapping
    fun findByUsernameOrEmail(@RequestParam(name = "q") query: String): ResponseEntity<UserDTO> {
        return ResponseEntity.status(HttpStatus.OK)
            .body(userService.findByUsernameOrEmail(query))
    }

    @GetMapping("/me")
    fun findAuthenticatedUser(principal: Principal): ResponseEntity<UserDTO> {
        return ResponseEntity.status(HttpStatus.OK)
            .body(userService.findAuthenticatedUser())
    }

    @GetMapping("/exists")
    fun existsByUsernameOrEmail(@RequestParam username: String?, @RequestParam email: String?): ResponseEntity<ExistsResponseDTO> {
        return ResponseEntity.status(HttpStatus.OK)
            .body(userService.existsByUsernameOrEmail(username, email))
    }

    @PostMapping
    fun createNewUserAccount(@RequestBody @Valid request: SignUpRequest): ResponseEntity<Unit> {
        userService.save(request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .build()
    }

    @PatchMapping("/plan")
    fun updatePlan(plan: Plan): ResponseEntity<Long> {
        return ResponseEntity.status(HttpStatus.OK)
            .body(userService.updatePlan(plan))
    }

    @PatchMapping(path = ["/edit"])
    fun edit(@RequestParam request: EditUserRequest, @RequestParam file: MultipartFile?): ResponseEntity<UserDTO> {
        return ResponseEntity.status(HttpStatus.OK)
            .body(userService.edit(request, file))
    }

}