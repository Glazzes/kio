package com.kio.services

import com.kio.dto.response.save.SavedUserDTO
import com.kio.dto.request.SignUpRequest
import com.kio.entities.UnitSummary
import com.kio.entities.User
import com.kio.repositories.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserService(val userRepository: UserRepository, val passwordEncoder: PasswordEncoder){

    fun save(signUpRequest: SignUpRequest): SavedUserDTO {
        val encodedPassword = passwordEncoder.encode(signUpRequest.password)

        val newUser = User(
            username = signUpRequest.username,
            password = encodedPassword,
            email = signUpRequest.email,
            profilePicture = null,
            unitSummary = UnitSummary()
        )

        val createdUser = userRepository.save(newUser)
        return SavedUserDTO(createdUser.id!!, createdUser.username, createdUser.email)
    }

    fun existsByEmail(email: String): Boolean {
        return userRepository.existsByEmail(email)
    }

    fun existsByUsername(username: String): Boolean {
        return userRepository.existsByUsername(username)
    }

}