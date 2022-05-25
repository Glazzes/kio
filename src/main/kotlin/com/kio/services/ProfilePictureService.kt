package com.kio.services

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import com.kio.configuration.properties.BucketConfigurationProperties
import com.kio.dto.response.StaticResponseDTO
import com.kio.entities.ProfilePicture
import com.kio.entities.User
import com.kio.repositories.ProfilePictureRepository
import com.kio.repositories.UserRepository
import com.kio.shared.exception.NotFoundException
import com.kio.shared.utils.SecurityUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Service
class ProfilePictureService (
    private val s3: AmazonS3,
    private val bucketProperties: BucketConfigurationProperties,
    private val userRepository: UserRepository,
    private val profilePictureRepository: ProfilePictureRepository,
){

    @Value("\${kio.default-pfp-key}") private lateinit var defaultProfilePictureKey: String

    fun save(file: MultipartFile) {
        val authenticatedUser = SecurityUtil.getAuthenticatedUser()
        val bucketKey = "${authenticatedUser.id!!}/${UUID.randomUUID()}"

        val metadata = ObjectMetadata().apply {
            contentType = file.contentType!!
            contentLength = file.size
        }

        s3.putObject(bucketProperties.profilePicturesBucket, bucketKey, file.inputStream, metadata)
        val profilePicture = ProfilePicture(bucketKey = bucketKey)

        if(authenticatedUser.profilePicture.bucketKey == defaultProfilePictureKey) {
            userRepository.save(authenticatedUser.apply { this.profilePicture = profilePicture })
            return
        }

        profilePictureRepository.save(authenticatedUser.profilePicture.apply { this.isActive = false })
        userRepository.save(authenticatedUser.apply { this.profilePicture = profilePicture })
    }

    fun findDefault(): StaticResponseDTO {
        val s3Picture = s3.getObject(bucketProperties.filesBucket, defaultProfilePictureKey)
        return StaticResponseDTO(s3Picture.objectMetadata.contentType, s3Picture.objectContent)
    }

    fun findMine(): StaticResponseDTO {
        val authenticatedUser = SecurityUtil.getAuthenticatedUser()
        val s3Picture = s3.getObject(bucketProperties.filesBucket, authenticatedUser.profilePicture.bucketKey)
        return StaticResponseDTO(s3Picture.objectMetadata.contentType, s3Picture.objectContent)
    }

    fun findById(id: String): StaticResponseDTO {
        val profilePicture = profilePictureRepository.findById(id)
            .orElseThrow { NotFoundException("Not profile picture was found with id $id") }

        val s3Picture = s3.getObject(bucketProperties.filesBucket, profilePicture.bucketKey)
        return StaticResponseDTO(s3Picture.objectMetadata.contentType, s3Picture.objectContent)
    }

    fun findByUserId(id: String): StaticResponseDTO {
        val user = this.findUser(id)
        val s3Picture = s3.getObject(bucketProperties.filesBucket, user.profilePicture.bucketKey)
        return StaticResponseDTO(s3Picture.objectMetadata.contentType, s3Picture.objectContent)
    }

    private fun findUser(id: String): User {
        return userRepository.findById(id)
            .orElseThrow { NotFoundException("Could not find user with id $id") }
    }

}