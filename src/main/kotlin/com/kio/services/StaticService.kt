package com.kio.services

import com.amazonaws.services.s3.AmazonS3
import com.kio.configuration.properties.BucketConfigurationProperties
import com.kio.dto.response.StaticResponseDTO
import com.kio.entities.enums.FileVisibility
import com.kio.entities.enums.Permission
import com.kio.repositories.FileRepository
import com.kio.repositories.FolderRepository
import com.kio.shared.exception.NotFoundException
import com.kio.shared.utils.PermissionValidator
import org.springframework.stereotype.Service

@Service
class StaticService(
    private val s3: AmazonS3,
    private val fileRepository: FileRepository,
    private val folderRepository: FolderRepository,
    private val buckets: BucketConfigurationProperties
){

    fun downloadFileById(fileId: String): StaticResponseDTO {
        val file = fileRepository.findById(fileId)
            .orElseThrow { NotFoundException("File with id $fileId does not exists") }

        val parentFolder = folderRepository.findById(file.parentFolder)
            .orElseThrow { NotFoundException("Folder with id ${file.parentFolder} does not exists") }

        if(file.visibility != FileVisibility.PUBLIC) {
            PermissionValidator.checkFolderPermissions(parentFolder, Permission.READ_ONLY)
        }

        val content = s3.getObject(buckets.filesBucket, file.bucketKey)
            .objectContent

        return StaticResponseDTO(file.contentType, content)
    }

}