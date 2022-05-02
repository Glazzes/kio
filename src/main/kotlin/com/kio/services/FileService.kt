package com.kio.services

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import com.kio.dto.response.find.FileDTO
import com.kio.dto.response.modify.RenamedEntityDTO
import com.kio.dto.response.save.SavedFileDTO
import com.kio.dto.request.GenericResourceRequest
import com.kio.entities.File
import com.kio.entities.AuditFileMetadata
import com.kio.entities.Folder
import com.kio.entities.enums.Permission
import com.kio.repositories.FileRepository
import com.kio.repositories.FolderRepository
import com.kio.shared.exception.NotFoundException
import com.kio.shared.utils.FileUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Service
@Transactional
class FileService(
    private val fileRepository: FileRepository,
    private val folderRepository: FolderRepository,
    private val s3: AmazonS3
){
    private val kioBucket = "files.kio.com"

    fun save(file: MultipartFile, parentFolderId: String): SavedFileDTO {
        val parentFolder = this.findFolderById(parentFolderId)
        PermissionValidator.checkResourcePermissions(parentFolder, Permission.CAN_CREATE)

        val s3Metadata = ObjectMetadata()
        s3Metadata.contentType = file.contentType
        s3Metadata.contentLength = file.size

        val key = "${parentFolder.id}/${UUID.randomUUID()}"
        s3.putObject(kioBucket, key, file.inputStream, s3Metadata)

        val fileNames = fileRepository.getFolderFilesNames(parentFolder.files)
        val validName = FileUtils.getValidName(file.originalFilename!!, fileNames)

        val fileToSave = File(
            name = validName,
            contentType = file.contentType ?: "UNKNOWN",
            size = file.size,
            bucketKey = key,
            parentFolder = parentFolderId,
            state = parentFolder.state,
            metadata = AuditFileMetadata(parentFolder.metadata.owner))

        val savedFile = fileRepository.save(fileToSave)

        return SavedFileDTO(
            id = savedFile.id,
            name = savedFile.name,
            size = savedFile.size,
            contentType = savedFile.contentType,
            createdAt = savedFile.metadata.createdAt!!)
    }


    fun findById(fileId: String): FileDTO {
        val file = this.findByIdInternal(fileId)
        val parentFolder = this.findFolderById(file.parentFolder)
        PermissionValidator.checkResourcePermissions(parentFolder, Permission.CAN_READ)

        return FileDTO(id = file.id!!, name = file.name, size = file.size, contentType = file.contentType)
    }

    fun rename(fileId: String, newName: String): RenamedEntityDTO {
        val file = this.findByIdInternal(fileId)
        val parentFolder = this.findFolderById(file.parentFolder)

        PermissionValidator.checkResourcePermissions(parentFolder, Permission.CAN_MODIFY)

        val from = file.name
        fileRepository.save(file)
        return RenamedEntityDTO(from, newName)
    }

    fun deleteById(fileId: String) {
        val fileToDelete = this.findByIdInternal(fileId)
        val parentFolder = this.findFolderById(fileToDelete.parentFolder)
        PermissionValidator.checkResourcePermissions(parentFolder, Permission.CAN_DELETE_FILE)
        fileRepository.delete(fileToDelete)
    }

    fun deleteMany(deleteManyRequest: GenericResourceRequest) {
        val parentFolder = this.findFolderById(deleteManyRequest.parentFolder)
        PermissionValidator.checkResourcePermissions(parentFolder, Permission.CAN_DELETE_FILE)

        for(fileId in deleteManyRequest.resources) {
            if(parentFolder.files.contains(fileId)) {
                fileRepository.deleteById(fileId)
            }
        }
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