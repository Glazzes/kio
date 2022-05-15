package com.kio.services

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.DeleteObjectsRequest
import com.kio.configuration.properties.BucketConfigurationProperties
import com.kio.dto.request.FileCopyRequest
import com.kio.dto.response.find.FileDTO
import com.kio.entities.File
import com.kio.entities.Folder
import com.kio.entities.enums.Permission
import com.kio.repositories.FileRepository
import com.kio.repositories.FolderRepository
import com.kio.shared.enums.FileCopyStrategy
import com.kio.shared.exception.NotFoundException
import com.kio.shared.utils.CopyUtil
import com.kio.shared.utils.FileUtils
import com.kio.shared.utils.PermissionValidatorUtil
import org.springframework.stereotype.Service
import java.util.*
import kotlin.collections.HashSet

@Service
class CopyService(
    private val fileRepository: FileRepository,
    private val folderRepository: FolderRepository,
    private val bucketConfigurationProperties: BucketConfigurationProperties,
    private val s3: AmazonS3
){

    fun copyFiles(copyRequest: FileCopyRequest): Collection<FileDTO> {
        val source = this.findFolderById(copyRequest.sourceId)
        val destination = this.findFolderById(copyRequest.destinationId)

        PermissionValidatorUtil.checkFolderPermissions(source, Permission.READ_WRITE)
        PermissionValidatorUtil.checkFolderPermissions(destination, Permission.READ_WRITE)

        val sourceFiles = fileRepository.findByIdIsIn(source.files)
        val destinationFiles = fileRepository.findByIdIsIn(destination.files)

        if(copyRequest.strategy == FileCopyStrategy.OVERWRITE) {
            return this.copyFilesWithOverwriteStrategy(destination, sourceFiles, destinationFiles)
        }

        return this.copyFilesWithRenameStrategy(destination, sourceFiles, destinationFiles)
    }

    private fun copyFilesWithRenameStrategy(
        destination: Folder,
        sourceFiles: Collection<File>,
        destinationFiles: Collection<File>
    ): Collection<FileDTO> {
        val destinationFilenames = destinationFiles.map { it.name }
        val newFiles: MutableSet<File> = HashSet()

        for(file in sourceFiles) {
            val newName = FileUtils.getValidName(file.name, destinationFilenames)
            val bucketKey = "${destination.id}/${UUID.randomUUID()}"

            val newFile = CopyUtil.cloneFile(file, destination, newName, bucketKey)
            newFiles.add(newFile)

            val s3Object = s3.getObject(bucketConfigurationProperties.filesBucket, file.bucketKey)
            s3.putObject(bucketConfigurationProperties.filesBucket, newFile.bucketKey, s3Object.objectContent, s3Object.objectMetadata)
        }

        return fileRepository.saveAll(newFiles)
            .map { FileDTO(it.id!!, it.name, it.size, it.contentType) }
    }

    private fun copyFilesWithOverwriteStrategy(
        destination: Folder,
        sourceFiles: Collection<File>,
        destinationFiles: Collection<File>
    ): Collection<FileDTO> {
        val sourceFileNames = sourceFiles.map { it.name }
        val filesToDelete = destinationFiles.filter { sourceFileNames.contains(it.name) }

        val deleteObjects = DeleteObjectsRequest(bucketConfigurationProperties.filesBucket)
        deleteObjects.keys = filesToDelete.map { DeleteObjectsRequest.KeyVersion(it.bucketKey) }

        val newFiles = mutableSetOf<File>()
        for(file in sourceFiles) {
            val bucketKey = "${destination.id}/${UUID.randomUUID()}"
            val newFile = CopyUtil.cloneFile(file, destination, file.name, bucketKey)
            newFiles.add(newFile)

            val s3Object = s3.getObject(bucketConfigurationProperties.filesBucket, file.bucketKey)
            s3.putObject(bucketConfigurationProperties.filesBucket, newFile.bucketKey, s3Object.objectContent, s3Object.objectMetadata)
        }

        fileRepository.deleteAll(filesToDelete)
        s3.deleteObjects(deleteObjects)
        return fileRepository.saveAll(newFiles)
            .map { FileDTO(it.id!!, it.name, it.size, it.contentType) }
    }

    private fun findFolderById(id: String): Folder {
        return folderRepository.findById(id)
            .orElseThrow { NotFoundException("Can not find folder with id $id") }
    }

}