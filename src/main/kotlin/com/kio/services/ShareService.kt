package com.kio.services

import com.amazonaws.services.kms.model.NotFoundException
import com.kio.dto.request.ShareRequest
import com.kio.repositories.FolderRepository
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import java.time.Duration

@Service
class ShareService(
    private val redisTemplate: RedisTemplate<String, Boolean>,
    private val fileService: FileService,
    private val folderRepository: FolderRepository
) {

    fun shareFile(shareRequest: ShareRequest) {
        val file = fileService.findByIdInternal(shareRequest.resource)
        val isOwner = PermissionValidator.isResourceOwner(file)
        if(!isOwner) {
            throw IllegalStateException("You can not shared a resource you do not own")
        }

        val duration = this.getDuration(shareRequest.timeUnit, shareRequest.duration)
        redisTemplate.opsForValue().set(file.id!!, true, duration)
    }

    fun shareFolder(shareRequest: ShareRequest) {
        val folder = folderRepository.findById(shareRequest.resource)
            .orElseThrow { NotFoundException("We could not found folder with id ${shareRequest.resource}") }
        val isOwner = PermissionValidator.isResourceOwner(folder)

        if(!isOwner) {
            throw IllegalStateException("You can not shared a resource you do not own")
        }

        val duration = this.getDuration(shareRequest.timeUnit, shareRequest.duration)
    }

    private fun getDuration(timeUnit: TimeUnit, duration: Long): Duration {
        if(duration < 0) throw IllegalStateException("No negative numbers, share duration must be bigger than one")

        val times = hashMapOf(
            TimeUnit.MINUTES to 60 * 24 * 7,
            TimeUnit.HOURS to 24 * 7,
            TimeUnit.DAYS to 7)

        val maxTime = times[timeUnit] ?: throw IllegalStateException("Share time must be specified in minutes, hours or days")
        if(duration > maxTime) throw IllegalStateException("Share duration exceeds 7 days")

        return when(timeUnit) {
            TimeUnit.MINUTES -> Duration.ofMinutes(duration)
            TimeUnit.HOURS -> Duration.ofHours(duration)
            TimeUnit.DAYS -> Duration.ofDays(duration)
            else -> Duration.ofSeconds(0)
        }
    }

}