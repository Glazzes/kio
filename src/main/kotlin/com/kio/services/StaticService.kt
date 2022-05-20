package com.kio.services

import com.amazonaws.services.s3.AmazonS3
import com.kio.configuration.properties.BucketConfigurationProperties
import com.kio.dto.response.StaticResponseDTO
import com.kio.entities.enums.FileVisibility
import com.kio.entities.enums.Permission
import com.kio.repositories.FileRepository
import com.kio.repositories.FolderRepository
import com.kio.shared.exception.NotFoundException
import com.kio.shared.utils.PermissionValidatorUtil
import org.springframework.stereotype.Service
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Service
class StaticService(
    private val s3: AmazonS3,
    private val fileRepository: FileRepository,
    private val folderRepository: FolderRepository,
    private val buckets: BucketConfigurationProperties
){

    fun downloadFileById(id: String): StaticResponseDTO {
        val file = fileRepository.findById(id)
            .orElseThrow { NotFoundException("File with id $id does not exists") }

        val parentFolder = folderRepository.findById(file.parentFolder)
            .orElseThrow { NotFoundException("Folder with id ${file.parentFolder} does not exists") }

        if(file.visibility != FileVisibility.PUBLIC) {
            PermissionValidatorUtil.checkFolderPermissions(parentFolder, Permission.READ_ONLY)
        }

        val content = s3.getObject(buckets.filesBucket, file.bucketKey)
            .objectContent

        return StaticResponseDTO(file.contentType, content)
    }

    fun downloadFolderById(id: String, response: HttpServletResponse) {

    }

}