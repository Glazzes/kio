package com.kio.services

import com.kio.dto.request.EditUserRequest
import com.kio.dto.request.SignUpRequest
import com.kio.dto.response.ExistsResponseDTO
import com.kio.dto.response.UserDTO
import com.kio.entities.User
import com.kio.mappers.UserMapper
import com.kio.repositories.UserRepository
import com.kio.shared.exception.BadRequestException
import com.kio.shared.exception.NotFoundException
import com.kio.shared.utils.SecurityUtil
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.validation.BindException
import org.springframework.validation.FieldError
import org.springframework.validation.MapBindingResult
import org.springframework.web.multipart.MultipartFile

@Service
class UserService(
    val userRepository: UserRepository,
    val passwordEncoder: PasswordEncoder,
    val profilePictureService: ProfilePictureService
){

    fun save(signUpRequest: SignUpRequest): UserDTO {
        val encodedPassword = passwordEncoder.encode(signUpRequest.password)

        val newUser = User (
            username = signUpRequest.username,
            password = encodedPassword,
            email = signUpRequest.email,
        )

        val createdUser = userRepository.save(newUser)
        return UserMapper.toUserDTO(createdUser)
    }

    fun existsByEmail(email: String): Boolean {
        return userRepository.existsByEmail(email)
    }

    fun edit(request: EditUserRequest, picture: MultipartFile?): UserDTO {
        this.validateEditRequest(request)
        val authenticatedUser = SecurityUtil.getAuthenticatedUser()

        if(picture !== null) {
            val pictureId = profilePictureService.save(picture)
            authenticatedUser.apply { profilePictureId = pictureId }
        }

        authenticatedUser.apply {
            if(request.password !== null) {
                password = passwordEncoder.encode(request.password)
            }

            username = request.username
            email = request.email
        }

        val updatedUser = userRepository.save(authenticatedUser)
        return UserMapper.toUserDTO(updatedUser)
    }

    private fun validateEditRequest(request: EditUserRequest) {
        var shouldThrow = false
        val binding = MapBindingResult(mutableMapOf<String, String>(), "map")
        val bindException = BindException(binding)

        if (request.username.length <= 3 || request.username.length >= 50) {
            shouldThrow = true
            bindException.addError(FieldError("map", "username", """
                * Username must be between 3 and 50 characters long
            """.trimIndent()))
        }

        val regex = Regex("(?:[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])")
        if(!request.email.matches(regex)) {
            shouldThrow = true
            bindException.addError(FieldError("map", "email", "* Invalid email, i.g user@kio.com"))
        }

        if(request.password !== null) {
            if(request.password.length < 8 || request.password.length > 100) {
                shouldThrow = true
                bindException.addError(FieldError("map", "password", """
                    * Password must be between 8 and 100 characters long
                """.trimIndent()))
            }

            val passwordRegex = Regex("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&-+=()])(?=\\\\S+\$).{8,100}\$")
            if(!request.password.matches(passwordRegex)) {
                shouldThrow = true
                bindException.addError(FieldError("map", "password", """
                    * Passwords must contain at one uppercase latter and one digit
                """.trimIndent()))
            }
        }

        if(shouldThrow) {
            throw bindException
        }
    }

    fun findByUsernameOrEmail(query: String): UserDTO {
        val user = userRepository.findByUsernameOrEmail(query, query) ?:
            throw NotFoundException("Could not find username with query \"$query\"")

        return UserMapper.toUserDTO(user)
    }

    fun existsByUsernameOrEmail(username: String?, email: String?): ExistsResponseDTO {
        if(username === null && email === null) {
            throw BadRequestException("Username and Email are null, at least one of them must not be null")
        }

        var existsByEmail = false
        var existsByUsername = false

        username?.let {
            existsByUsername = userRepository.existsByUsername(it)
        }

        email?.let {
            existsByEmail = userRepository.existsByEmail(it)
        }

        return ExistsResponseDTO(existsByUsername, existsByEmail)
    }

}