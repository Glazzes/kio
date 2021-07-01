package com.kio.services

import com.kio.entities.User
import com.kio.entities.models.SignUpRequest
import com.kio.repositories.UserRepository
import com.kio.shared.exception.BadRequestException
import com.kio.shared.exception.UserNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
@Transactional
class UserService(val userRepository: UserRepository, val passwordEncoder: PasswordEncoder){

    fun save(signUpRequest: SignUpRequest){
        if(userRepository.existsByEmail(signUpRequest.email)){
            throw BadRequestException("Email is already in use.")
        }

        if(userRepository.existsByUsername(signUpRequest.username)){
            throw BadRequestException("Username is already in use.")
        }

        val newUser = User(
            username = signUpRequest.username,
            password = signUpRequest.password,
            email = signUpRequest.email
        )

        userRepository.save(newUser)
    }

    fun deleteById(id: String){
        if(userRepository.existsById(id)){
            userRepository.deleteById(id)
            return
        }

        throw BadRequestException("Can not delete user with id $id because it does not exists")
    }

}