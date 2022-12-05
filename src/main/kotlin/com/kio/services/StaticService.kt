package com.kio.services

import com.amazonaws.AmazonServiceException
import com.amazonaws.services.s3.AmazonS3
import com.kio.configuration.properties.BucketConfigurationProperties
import com.kio.dto.response.StaticResponseDTO
import com.kio.entities.File
import com.kio.entities.Folder
import com.kio.entities.enums.FileVisibility
import com.kio.entities.enums.Permission
import com.kio.repositories.FileRepository
import com.kio.repositories.FolderRepository
import com.kio.shared.exception.NotFoundException
import com.kio.shared.utils.FileUtils
import com.kio.shared.utils.PermissionValidatorUtil
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

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
            PermissionValidatorUtil.verifyFolderPermissions(parentFolder, Permission.READ_ONLY)
        }

        try{
            val s3Object = s3.getObject(buckets.filesBucket, file.bucketKey) ?:
                throw NotFoundException("This file does not exists")

            val body = FileUtils.getStreamingResponseBodyFromObjectContent(s3Object.objectContent)
            return StaticResponseDTO(file.contentType, body)
        }catch (e: AmazonServiceException) {
            throw NotFoundException(e.message)
        }
    }

    fun downloadThumbnail(fileId: String): StaticResponseDTO {
        val file = fileRepository.findById(fileId)
            .orElseThrow { NotFoundException("File with id $fileId does not exists") }

        if(file.details.thumbnailKey == null) {
            throw NotFoundException("File with id $fileId does not have thumbnail")
        }

        try{
            val s3Object = s3.getObject(buckets.filesBucket, file.details.thumbnailKey) ?:
                throw NotFoundException("This file does not exists")

            val response = FileUtils.getStreamingResponseBodyFromObjectContent(s3Object.objectContent)
            return StaticResponseDTO(file.contentType, response)
        }catch (e: AmazonServiceException) {
            throw NotFoundException(e.message)
        }
    }

    fun downloadFolderById(id: String): StaticResponseDTO {
        val folder = folderRepository.findById(id)
            .orElseThrow { NotFoundException("Could not found folder with id $id") }

        PermissionValidatorUtil.verifyFolderPermissions(folder, Permission.READ_ONLY)
        // response.setHeader("Content-Disposition", "attachment;filename=${folder.name}.zip")
        val body = this.writeZipFile(folder)
        return StaticResponseDTO("application/zip", body)
    }

    private fun writeZipFile(folder: Folder) = StreamingResponseBody { out ->
        out.use {
            val zipOutputStream = ZipOutputStream(it)
            zipOutputStream.use { zos ->
                zipFoldersRecursively("", folder, zos)
            }
        }
    }

    private fun zipFoldersRecursively(rootName: String, folder: Folder, zos: ZipOutputStream) {
        val subFolders = folderRepository.findByIdIsIn(folder.subFolders)
        val files = fileRepository.findByIdIsIn(folder.files)

        val currentFolderName = "${rootName}/${folder.name}"
        files.forEach { this.writeFileContentToZipEntry(currentFolderName, it, zos) }

        for(subFolder in subFolders) {
            this.zipFoldersRecursively(currentFolderName, subFolder, zos)
        }
    }

    private fun writeFileContentToZipEntry(rootName: String, file: File, zos: ZipOutputStream) {
        val content = s3.getObject(buckets.filesBucket, file.bucketKey)
            .objectContent
            .delegateStream

        content.use {
            val entry = ZipEntry("${rootName}/${file.name}")
            zos.putNextEntry(entry)

            // I have tried many values but only 4 and 8 will not turn out in a "corrupted" file
            val bytes = ByteArray(1024)
            while (it.read(bytes) != -1) {
                zos.write(bytes)
            }

            zos.closeEntry()
        }
    }

}