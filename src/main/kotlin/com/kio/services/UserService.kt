package com.kio.services

import com.kio.dto.request.SignUpRequest
import com.kio.dto.response.UserDTO
import com.kio.entities.ProfilePicture
import com.kio.entities.User
import com.kio.mappers.UserMapper
import com.kio.repositories.UserRepository
import com.kio.shared.exception.NotFoundException
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    val userRepository: UserRepository,
    val folderService: FolderService,
    val passwordEncoder: PasswordEncoder,
){
    @Value("\${kio.default-pfp-key}") private lateinit var defaultProfilePictureKey: String

    fun save(signUpRequest: SignUpRequest): UserDTO {
        val encodedPassword = passwordEncoder.encode(signUpRequest.password)

        val newUser = User (
            username = signUpRequest.username,
            password = encodedPassword,
            email = signUpRequest.email,
            profilePicture = ProfilePicture(id="default", bucketKey = defaultProfilePictureKey)
        )

        val createdUser = userRepository.save(newUser)
        folderService.saveRootFolderForNewUser(createdUser)
        return UserMapper.toUserDTO(newUser)
    }

    fun existsByEmail(email: String): Boolean {
        return userRepository.existsByEmail(email)
    }

    fun findByUsernameOrEmail(query: String): UserDTO {
        val user = userRepository.findByUsernameOrEmail(query) ?:
            throw NotFoundException("Could not find user by query $query")

        return UserMapper.toUserDTO(user)
    }

}