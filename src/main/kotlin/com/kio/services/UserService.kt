package com.kio.services

import com.kio.dto.request.SignUpRequest
import com.kio.dto.response.UserDTO
import com.kio.entities.UnitSummary
import com.kio.entities.User
import com.kio.mappers.UserMapper
import com.kio.repositories.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    val userRepository: UserRepository,
    val folderService: FolderService,
    val passwordEncoder: PasswordEncoder,
){

    fun save(signUpRequest: SignUpRequest): UserDTO {
        val encodedPassword = passwordEncoder.encode(signUpRequest.password)

        val newUser = User(
            username = signUpRequest.username,
            password = encodedPassword,
            email = signUpRequest.email,
            unitSummary = UnitSummary(),
        )

        val createdUser = userRepository.save(newUser)

        folderService.saveRootFolderForNewUser(createdUser)
        return UserMapper.toUserDTO(newUser)
    }

    fun existsByEmail(email: String): Boolean {
        return userRepository.existsByEmail(email)
    }

}