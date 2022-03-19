package com.kio.services

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import com.kio.dto.response.find.FileDTO
import com.kio.dto.response.modify.RenamedEntityDTO
import com.kio.dto.response.save.SavedFileDTO
import com.kio.dto.request.GenericResourceRequest
import com.kio.entities.File
import com.kio.entities.AuditFileMetadata
import com.kio.entities.enums.Permission
import com.kio.repositories.FileRepository
import com.kio.repositories.FolderRepository
import com.kio.shared.exception.NotFoundException
import com.kio.shared.utils.FileUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.lang.IllegalStateException
import java.util.*
import kotlin.UnsupportedOperationException

@Service
@Transactional
class FileService(
    private val fileRepository: FileRepository,
    private val folderRepository: FolderRepository,
    private val s3: AmazonS3
){
    private val kioBucket = "files.kio.com"

    fun save(file: MultipartFile, parentFolderId: String): SavedFileDTO {
        val parentFolder = folderRepository.findById(parentFolderId)
            .orElseThrow { NotFoundException("Can not save a file to a folder that does not exists $parentFolderId") }

        val s3Metadata = ObjectMetadata()
        s3Metadata.contentType = file.contentType
        s3Metadata.contentLength = file.size

        val key = "glaze/${parentFolder.id}/${UUID.randomUUID()}"
        s3.putObject(kioBucket, key, file.inputStream, s3Metadata)

        val fileNames = fileRepository.getSubFileNames(parentFolder.files)
        val validName = FileUtils.getValidName(file.originalFilename!!, fileNames)
        val savedFile = fileRepository.save(
            File(
            name = validName,
            contentType = file.contentType ?: "N/A",
            size = file.size,
            bucketKey = key,
            parentFolder = parentFolderId,
            url = s3.getUrl(kioBucket, key).toString(),
            metadata = AuditFileMetadata("glaze")
            )
        )

        return SavedFileDTO(
            id = savedFile.id,
            name = savedFile.name,
            size = savedFile.size,
            contentType = savedFile.contentType,
            createdAt = savedFile.metadata.createdAt!!)
    }


    fun findById(fileId: String): FileDTO {
        val file = fileRepository.findById(fileId)
            .orElseThrow { NotFoundException("We could not find file with id $fileId") }

        val parentFolder = folderRepository.findById(file.parentFolder)
            .orElseThrow { IllegalStateException("This file does not belong to any folder") }

        val canRename = PermissionValidator.canPerformOperation(parentFolder, Permission.CAN_READ)
        if(!canRename) throw UnsupportedOperationException("You are not allowed to view this file")

        return FileDTO(id = file.id!!, name = file.name, size = file.size, contentType = file.contentType)
    }

    fun findByIdInternal(fileId: String): File {
        return fileRepository.findById(fileId)
            .orElseThrow { NotFoundException("We could not find file with id $fileId") }
    }

    fun rename(fileId: String, newName: String): RenamedEntityDTO {
        val file = fileRepository.findById(fileId)
            .orElseThrow { NotFoundException("You can not delete a file that does not exists $fileId") }

        val parentFolder = folderRepository.findById(file.parentFolder)
            .orElseThrow { IllegalStateException("This file does not belong to any folder") }

        val canRename = PermissionValidator.canPerformOperation(parentFolder, Permission.CAN_MODIFY)
        if(!canRename) throw UnsupportedOperationException("You are not allowed to rename this file")

        val from = file.name
        fileRepository.save(file)
        return RenamedEntityDTO(from, newName)
    }

    fun deleteById(fileId: String) {
        val fileToDelete = fileRepository.findById(fileId)
            .orElseThrow { NotFoundException("You can not delete a file that does not exists $fileId") }

        val parentFolder = folderRepository.findById(fileToDelete.parentFolder)
            .orElseThrow { IllegalStateException("You can not delete a file of folder that does not exists") }

        val canDelete = PermissionValidator.canPerformOperation(parentFolder, Permission.CAN_DELETE_FILE)
        if(!canDelete) throw UnsupportedOperationException("You are not allowed to delete this file")

        fileRepository.delete(fileToDelete)
    }

    fun deleteMany(deleteManyRequest: GenericResourceRequest) {
        val parentFolder = folderRepository.findById(deleteManyRequest.parentFolder)
            .orElseThrow { NotFoundException("You can not delete files from a folder that does not exists") }

        val canDelete = PermissionValidator.canPerformOperation(parentFolder, Permission.CAN_DELETE_FILE)
        if(!canDelete) throw UnsupportedOperationException("You are not allowed to delete the files")

        for(fileId in deleteManyRequest.resources) {
            if(parentFolder.files.contains(fileId)) fileRepository.deleteById(fileId)
        }
    }

}