package com.kio.services

import com.kio.dto.RenamedEntityDTO
import com.kio.dto.create.CreatedFileDTO
import com.kio.dto.find.FileDTO
import com.kio.dto.find.UserDTO
import com.kio.entities.File
import com.kio.repositories.FileRepository
import com.kio.shared.exception.AssociationException
import com.kio.shared.exception.NotFoundException
import com.kio.shared.utils.DiskUtil
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.*
import java.util.*
import javax.transaction.Transactional

@Service
@Transactional
class FileService (
    private val fileRepository: FileRepository,
    private val folderService: FolderService
    ){

    @Transactional(rollbackOn = [IOException::class, RuntimeException::class])
    fun save(file: MultipartFile, parentFolderId: String): CreatedFileDTO{
        val fileInputStream = file.inputStream as FileInputStream
        val filename = Optional.ofNullable(file.originalFilename)
            .orElseThrow { IllegalStateException("Can no save file because the filename must not be null") }

        val newFile = File(
            filename = filename,
            size = file.size,
            originalFilename = filename,
            mimeType = file.contentType
        )
        DiskUtil.saveFileToDisk(fileInputStream, filename)

        val createdFile = fileRepository.save(newFile)

        folderService.findById(parentFolderId)
            .orElseThrow { NotFoundException("Can not save file $filename because folder $parentFolderId does ot exists") }
            .apply { files.add(createdFile) }

        return CreatedFileDTO(
            createdFile.id,
            createdFile.filename,
            createdFile.size,
            createdFile.createdAt
        )
    }

    fun findById(id: String): FileDTO {
        val file = fileRepository.findById(id)
            .orElseThrow { NotFoundException("Can not find file with $id because it does not exists") }

        val createdBy = file.createdBy ?: throw AssociationException("File with $id is not associated with a user")
        return FileDTO(
            file.id,
            file.filename,
            file.size,
            file.lastModified,
            UserDTO(createdBy.id, createdBy.nickname, createdBy.profilePicture)
        )
    }

    fun renameFile(fileId: String, newFilename: String): RenamedEntityDTO {
        val file = fileRepository.findById(fileId)
            .orElseThrow {throw NotFoundException("Can not rename file with $fileId because it does not exists.")}

        file.apply { filename =  newFilename}
        return RenamedEntityDTO(file.filename, file.lastModified)
    }

    @Transactional(rollbackOn = [FileNotFoundException::class, RuntimeException::class])
    fun deleteFileById(id: String){
        val file = fileRepository.findById(id)
            .orElseThrow { NotFoundException("Can not delete file with $id because it does not exists") }

        val createdBy = file.createdBy ?: throw AssociationException("This file is not associated with an user")
        createdBy.apply { spaceUsed-=file.size }

        DiskUtil.deleteFileFromDisk(file.originalFilename)
        fileRepository.deleteById(id)
    }

    fun deleteFile(file: File){
        fileRepository.delete(file)
        DiskUtil.deleteFileFromDisk(file.originalFilename)
    }

}