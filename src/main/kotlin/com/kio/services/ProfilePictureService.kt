package com.kio.services

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import com.kio.configuration.properties.BucketConfigurationProperties
import com.kio.dto.response.StaticResponseDTO
import com.kio.entities.User
import com.kio.repositories.UserRepository
import com.kio.shared.exception.NotFoundException
import com.kio.shared.utils.FileUtils
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
){

    @Value("\${kio.default-pfp-key}") private lateinit var defaultProfilePictureKey: String

    fun save(file: MultipartFile): String {
        val authenticatedUser = SecurityUtil.getAuthenticatedUser()
        val pictureId = UUID.randomUUID().toString()
        val bucketKey = "${authenticatedUser.id!!}/${pictureId}"

        val metadata = ObjectMetadata().apply {
            contentType = file.contentType!!
            contentLength = file.size
        }

        s3.putObject(bucketProperties.profilePicturesBucket, bucketKey, file.inputStream, metadata)

        if(authenticatedUser.profilePictureId !== null) {
            s3.deleteObject(bucketProperties.profilePicturesBucket, authenticatedUser.profilePictureId)
        }

        return pictureId
    }

    fun findById(id: String): StaticResponseDTO {
        val user = this.findUser(id)
        val s3Picture = s3.getObject(bucketProperties.profilePicturesBucket, user.profilePictureId)
        val body = FileUtils.getStreamingResponseBodyFromObjectContent(s3Picture.objectContent)
        return StaticResponseDTO(s3Picture.objectMetadata.contentType, body)
    }

    fun findByUserId(userId: String, id: String): StaticResponseDTO {
        val user = this.findUser(userId)

        val pictureId = "${user.id}/${user.profilePictureId}"
        val s3Picture = s3.getObject(bucketProperties.profilePicturesBucket, pictureId)
        val body = FileUtils.getStreamingResponseBodyFromObjectContent(s3Picture.objectContent)
        return StaticResponseDTO(s3Picture.objectMetadata.contentType, body)
    }

    private fun findUser(id: String): User {
        return userRepository.findById(id)
            .orElseThrow { NotFoundException("Could not find user with id $id") }
    }

}