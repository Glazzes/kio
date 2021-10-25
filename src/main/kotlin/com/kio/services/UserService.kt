package com.kio.services

import com.kio.dto.create.CreatedUserDTO
import com.kio.entities.User
import com.kio.entities.models.SignUpRequest
import com.kio.repositories.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

@Service
@Transactional
class UserService(val userRepository: UserRepository, val passwordEncoder: PasswordEncoder){

    fun save(signUpRequest: SignUpRequest): CreatedUserDTO {
        val encodedPassword = passwordEncoder.encode(signUpRequest.password)

        val newUser = User(
            username = signUpRequest.username,
            password = encodedPassword,
            email = signUpRequest.email,
            nickname = signUpRequest.username
        )

        val createdUser = userRepository.save(newUser)
        return CreatedUserDTO(
            createdUser.username,
            createdUser.username,
            createdUser.spaceUsed,
            createdUser.profilePicture
        )
    }

    fun findByUsername(username: String): Optional<User>{
        return userRepository.findById(username)
    }

    fun existsByEmail(email: String): Boolean {
        return userRepository.existsByEmail(email)
    }

    fun existsByUsername(username: String): Boolean {
        return userRepository.existsByUsername(username)
    }

}