package com.kio.services

import com.kio.dto.request.SignUpRequest
import com.kio.dto.response.ExistsResponseDTO
import com.kio.dto.response.UserDTO
import com.kio.entities.ProfilePicture
import com.kio.entities.User
import com.kio.mappers.UserMapper
import com.kio.repositories.UserRepository
import com.kio.shared.exception.BadRequestException
import com.kio.shared.exception.NotFoundException
import com.kio.shared.utils.SecurityUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
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

    fun edit(request: SignUpRequest, picture: MultipartFile?): UserDTO {
        val authenticatedUser = SecurityUtil.getAuthenticatedUser()

        picture?.let { profilePictureService.save(it) }

        authenticatedUser.apply {
            username = request.username
            password = passwordEncoder.encode(request.password)
            email = request.email
        }

        val updatedUser = userRepository.save(authenticatedUser)
        return UserMapper.toUserDTO(updatedUser)
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