package com.kio.services

import com.amazonaws.services.kms.model.NotFoundException
import com.amazonaws.services.s3.AmazonS3
import com.kio.dto.response.StaticResponseDTO
import com.kio.dto.request.ShareRequest
import com.kio.entities.SharedResource
import com.kio.repositories.FolderRepository
import com.kio.shared.enums.ResourceType
import com.kio.shared.utils.PermissionValidatorUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import java.time.Duration
import java.util.UUID

@Service
class SharingService(
    private val redisTemplate: RedisTemplate<String, SharedResource>,
    private val fileService: FileService,
    private val folderRepository: FolderRepository,
    private val s3: AmazonS3
) {
    @Value("\${aws.s3.files-bucket}") private lateinit var filesBucket: String

    fun shareFile(shareRequest: ShareRequest): String {
        val file = fileService.findByIdInternal(shareRequest.resourceId)
        PermissionValidatorUtil.isResourceOwner(file)

        val randomKey = UUID.randomUUID().toString()
        val sharedResource = SharedResource(file.id!!, ResourceType.FILE)
        val duration = this.getDuration(shareRequest.timeUnit, shareRequest.duration)
        redisTemplate.opsForValue().set(randomKey, sharedResource, duration)
        return "http://localhost:8080/api/v1/sharing/file/$randomKey"
    }

    fun shareFolder(shareRequest: ShareRequest) {
        val folder = folderRepository.findById(shareRequest.resourceId)
            .orElseThrow { NotFoundException("We could not found folder with id ${shareRequest.resourceId}") }
        PermissionValidatorUtil.isResourceOwner(folder)

        val duration = this.getDuration(shareRequest.timeUnit, shareRequest.duration)
    }

    fun findById(sharedId: String): StaticResponseDTO {
        val sharedResource = redisTemplate.opsForValue().get(sharedId) ?:
            throw NotFoundException("There's no shared file with this id $sharedId")

        val file = fileService.findByIdInternal(sharedResource.resourceId)
        val content = s3.getObject(filesBucket, file.bucketKey)
            .objectContent

        return StaticResponseDTO(file.contentType, content)
    }

    private fun getDuration(timeUnit: TimeUnit, duration: Long): Duration {
        if(duration < 0) throw IllegalStateException("No negative numbers, share duration must be bigger than one")

        val times = hashMapOf(
            TimeUnit.MINUTES to 60 * 24 * 3,
            TimeUnit.HOURS to 24 * 3,
            TimeUnit.DAYS to 3)

        val maxTime = times[timeUnit] ?: throw IllegalStateException("Share time must be specified in minutes, hours or days")
        if(duration > maxTime) throw IllegalStateException("Share duration exceeds 3 days")

        return when(timeUnit) {
            TimeUnit.MINUTES -> Duration.ofMinutes(duration)
            TimeUnit.HOURS -> Duration.ofHours(duration)
            TimeUnit.DAYS -> Duration.ofDays(duration)
            else -> Duration.ofSeconds(0)
        }
    }

}