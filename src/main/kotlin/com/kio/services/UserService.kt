package com.kio.services

import com.kio.entities.User
import com.kio.entities.models.SignUpRequest
import com.kio.repositories.UserRepository
import com.kio.shared.exception.BadRequestException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

@Service
@Transactional
class UserService(val userRepository: UserRepository, val passwordEncoder: PasswordEncoder){

    fun save(signUpRequest: SignUpRequest){
        val encodedPassword = passwordEncoder.encode(signUpRequest.password)

        val newUser = User(
            username = signUpRequest.username,
            password = encodedPassword,
            email = signUpRequest.email
        )

        userRepository.save(newUser)
    }

    fun findByUsername(username: String): Optional<User>{
        return userRepository.findById(username)
    }

    fun existsByEmail(email: String): Boolean {
        return userRepository.existsByEmail(email)
    }

    fun deleteById(id: String){
        if(userRepository.existsById(id)){
            userRepository.deleteById(id)
            return
        }

        throw BadRequestException("Can not delete user with id $id because it does not exists")
    }

}