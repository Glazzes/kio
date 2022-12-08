package com.kio.services

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.CopyObjectRequest
import com.kio.configuration.properties.BucketConfigurationProperties
import com.kio.dto.request.file.FileCopyRequest
import com.kio.dto.response.FileDTO
import com.kio.dto.response.FolderDTO
import com.kio.entities.File
import com.kio.entities.Folder
import com.kio.entities.enums.Permission
import com.kio.mappers.FileMapper
import com.kio.mappers.FolderMapper
import com.kio.repositories.FileRepository
import com.kio.repositories.FolderRepository
import com.kio.shared.exception.BadRequestException
import com.kio.shared.exception.InsufficientStorageException
import com.kio.shared.exception.NotFoundException
import com.kio.shared.utils.PermissionValidatorUtil
import org.springframework.stereotype.Service
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet

@Service
class CopyService(
    private val fileRepository: FileRepository,
    private val folderRepository: FolderRepository,
    private val bucketConfigurationProperties: BucketConfigurationProperties,
    private val copyUtilService: CopyUtilService,
    private val spaceService: SpaceService,
    private val s3: AmazonS3
){

    fun copyFolders(copyRequest: FileCopyRequest): Collection<FolderDTO> {
        val source = this.findFolderById(copyRequest.from)

        if (!source.subFolders.containsAll(copyRequest.items)) {
            throw BadRequestException("At least one of the folder does not belong to source.")
        }

        val destination = this.findFolderById(copyRequest.to)
        val foldersToCopy = folderRepository.findByIdIsIn(copyRequest.items)

        var operationSize: Long = 0
        for(folder in foldersToCopy) {
            val size = spaceService.calculateFolderSize(folder)
            operationSize += size
        }

        val canCopy = spaceService.hasEnoughStorageToPerformOperation(destination, operationSize)
        if(!canCopy) {
            throw InsufficientStorageException("Owner of this unit has ran out space")
        }

        val folderCopies = folderRepository.findByIdIsIn(copyRequest.items)
            .map { copyUtilService.cloneFolder(it, destination) }

        val savedFolderCopies = folderRepository.saveAll(folderCopies)
        this.performCopyFolders(savedFolderCopies)

        folderRepository.save(destination.apply {
            subFolders.addAll(savedFolderCopies.map { it.id!! })
        })

        return savedFolderCopies.map { FolderMapper.toFolderDTO(it) }
    }

    private fun performCopyFolders(foldersToCopy: Collection<Folder>) {
        for (copy in foldersToCopy) {
            val fileCopies = fileRepository.findByIdIsIn(copy.files)
                .map {
                    val bucketKey = "${copy.id}/${UUID.randomUUID()}"

                    val copyObjectRequest = CopyObjectRequest().apply {
                        destinationBucketName = bucketConfigurationProperties.filesBucket
                        sourceBucketName = bucketConfigurationProperties.filesBucket
                        sourceKey = it.bucketKey
                        destinationKey = bucketKey
                    }

                    s3.copyObject(copyObjectRequest)

                    copyUtilService.cloneFile(it, copy, bucketKey)
                }

            val savedFileCopies = fileRepository.saveAll(fileCopies)
            val folderCopies = folderRepository.findByIdIsIn(copy.subFolders)
                .map { copyUtilService.cloneFolder(it, copy) }

            val savedFolderCopies = folderRepository.saveAll(folderCopies)

            val fileIds = savedFileCopies.map { it.id!! }.toMutableSet()
            val folderIds = savedFolderCopies.map { it.id!! }.toMutableSet()

            copy.apply {
                files = fileIds
                subFolders = folderIds
            }

            folderRepository.save(copy)
            this.performCopyFolders(savedFolderCopies)
        }
    }


    fun copyFiles(copyRequest: FileCopyRequest): Collection<FileDTO> {
        val source = this.findFolderById(copyRequest.from)
        val destination = this.findFolderById(copyRequest.to)

        this.verifyCopyFilePermissions(source, destination, copyRequest.items)

        val filesToCopy = fileRepository.findByIdIsIn(copyRequest.items)
        val operationSize = filesToCopy.sumOf { it.size }

        val canCopy = spaceService.hasEnoughStorageToPerformOperation(destination, operationSize)
        if(!canCopy) {
            throw InsufficientStorageException("Owner of this folder has ran out of space")
        }

        return this.performCopyOperation(destination, filesToCopy)
    }

    private fun verifyCopyFilePermissions(source: Folder, destination: Folder, files: Collection<String>) {
        if(!source.files.containsAll(files)) {
            throw BadRequestException("Some of the files to copy do not belong to the source folder")
        }

        PermissionValidatorUtil.verifyFolderPermissions(source, Permission.READ_WRITE)
        PermissionValidatorUtil.verifyFolderPermissions(destination, Permission.READ_WRITE)
    }

    private fun performCopyOperation(
        destination: Folder,
        filesToCopy: Collection<File>,
    ): Collection<FileDTO> {
        val filesToSave: MutableSet<File> = HashSet()
        val copySize = filesToCopy.sumOf { it.size }

        for(file in filesToCopy) {
            val bucketKey = "${destination.id}/${UUID.randomUUID()}"

            val newFile = copyUtilService.cloneFile(file, destination, bucketKey)
            filesToSave.add(newFile)

            val copyObjectRequest = CopyObjectRequest().apply {
                destinationBucketName = bucketConfigurationProperties.filesBucket
                sourceBucketName = bucketConfigurationProperties.filesBucket
                sourceKey = file.bucketKey
                destinationKey = bucketKey
            }

            s3.copyObject(copyObjectRequest)
        }

        val savedFiles = fileRepository.saveAll(filesToSave)
        destination.apply {
            summary.files += filesToSave.size
            summary.size += copySize
            files.addAll(savedFiles.map { it.id!! })
        }

        folderRepository.save(destination)

        return savedFiles.map { FileMapper.toFileDTO(it) }
    }

    private fun findFolderById(id: String): Folder {
        return folderRepository.findById(id)
            .orElseThrow { NotFoundException("Can not find folder with id $id") }
    }

}