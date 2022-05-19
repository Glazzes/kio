package com.kio.services

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.DeleteObjectsRequest
import com.amazonaws.services.s3.model.ObjectMetadata
import com.kio.dto.request.file.FileDeleteRequest
import com.kio.dto.request.file.FileEditRequest
import com.kio.dto.response.FileDTO
import com.kio.dto.response.modify.RenamedEntityDTO
import com.kio.entities.File
import com.kio.entities.details.FileDetails
import com.kio.entities.details.FileMetadata
import com.kio.entities.Folder
import com.kio.entities.enums.Permission
import com.kio.mappers.FileMapper
import com.kio.repositories.FileRepository
import com.kio.repositories.FolderRepository
import com.kio.shared.exception.IllegalOperationException
import com.kio.shared.exception.NotFoundException
import com.kio.shared.utils.FileUtils
import com.kio.shared.utils.PermissionValidatorUtil
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.*
import kotlin.collections.ArrayList

@Service
class FileService(
    private val fileRepository: FileRepository,
    private val folderRepository: FolderRepository,
    private val s3: AmazonS3
){
    private val kioBucket = "files.kio.com"

    fun save(parentFolderId: String, files: List<MultipartFile>): Collection<FileDTO> {
        val parentFolder = this.findFolderById(parentFolderId)
        PermissionValidatorUtil.checkFolderPermissions(parentFolder, Permission.READ_WRITE)

        val filesToSave: MutableList<File> = ArrayList()
        val parentFolderNames = fileRepository.getFolderFilesNames(parentFolder.files)

        for(file in files) {
            val key = "${parentFolder.id}/${UUID.randomUUID()}"

            val s3Metadata = ObjectMetadata()
            s3Metadata.contentType = file.contentType
            s3Metadata.contentLength = file.size

            s3.putObject(kioBucket, key, file.inputStream, s3Metadata)

            val validName = FileUtils.getValidName(file.originalFilename!!, parentFolderNames.map { it.getName() })

            val fileToSave = File(
                name = validName,
                details = FileDetails(),
                contentType = file.contentType ?: "UNKNOWN",
                size = file.size,
                bucketKey = key,
                parentFolder = parentFolderId,
                visibility = parentFolder.visibility,
                metadata = FileMetadata(parentFolder.metadata.ownerId)
            )

            filesToSave.add(fileToSave)
        }

        val savedFiles = fileRepository.saveAll(filesToSave)
        folderRepository.save(parentFolder.apply {
            this.files.addAll(savedFiles.map{ it.id!! })
            this.size += filesToSave.sumOf { it.size }
        })

        return savedFiles.map { FileMapper.toFileDTO(it) }
    }

    fun findById(fileId: String): FileDTO {
        val file = this.findByIdInternal(fileId)
        val parentFolder = this.findFolderById(file.parentFolder)
        PermissionValidatorUtil.checkFolderPermissions(parentFolder, Permission.READ_ONLY)

        return FileMapper.toFileDTO(file)
    }

    fun edit(id: String, request: FileEditRequest): FileDTO {
        val file = this.findByIdInternal(id)
        val parentFolder = this.findFolderById(file.parentFolder)

        PermissionValidatorUtil.checkFolderPermissions(parentFolder, Permission.READ_WRITE)

        val parentFilenames = fileRepository.findFilesNamesByParentId(file.parentFolder)
            .map { it.getName() }

        file.apply {
            this.name = FileUtils.getValidName(request.name, parentFilenames)
            this.visibility = request.visibility
        }

        return FileMapper.toFileDTO(file)
    }

    fun rename(fileId: String, newName: String): RenamedEntityDTO {
        val file = this.findByIdInternal(fileId)
        val parentFolder = this.findFolderById(file.parentFolder)

        PermissionValidatorUtil.checkFolderPermissions(parentFolder, Permission.READ_WRITE, Permission.MODIFY)

        fileRepository.save(file.apply { name = newName })
        return RenamedEntityDTO(file.name, newName)
    }

    fun deleteAll(deleteRequest: FileDeleteRequest) {
        val parentFolder = this.findFolderById(deleteRequest.from)

        if(!parentFolder.files.containsAll(deleteRequest.files)) {
            throw IllegalOperationException("At least one of the files does not belong to this folder ${deleteRequest.from}")
        }

        PermissionValidatorUtil.checkFolderPermissions(parentFolder, Permission.READ_WRITE, Permission.DELETE)
        val filesToDelete = fileRepository.findByIdIsIn(deleteRequest.files)
        val filesToDeleteSize = filesToDelete.sumOf { it.size }

        val deleteObjects = DeleteObjectsRequest(kioBucket).apply {
            keys = filesToDelete.map { DeleteObjectsRequest.KeyVersion(it.bucketKey) }
        }

        folderRepository.save(parentFolder.apply {
            files.removeAll(deleteRequest.files.toSet())
            size -= filesToDeleteSize
        })

        fileRepository.deleteAll(filesToDelete)
        s3.deleteObjects(deleteObjects)
    }

    private fun findFolderById(id: String): Folder {
        return folderRepository.findById(id)
            .orElseThrow { NotFoundException("We could not found folder with id $id") }
    }

    fun findByIdInternal(fileId: String): File {
        return fileRepository.findById(fileId)
            .orElseThrow { NotFoundException("We could not find file with id $fileId") }
    }

}