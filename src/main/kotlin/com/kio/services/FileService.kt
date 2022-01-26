package com.kio.services

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectResult
import com.kio.configuration.aws.AwsProperties
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
    private val folderService: FolderService,
    private val amazons3: AmazonS3,
    private val awsProperties: AwsProperties
){
    data class FileInfo(
        val filename: String,
        val url: String,
        val awsResult: PutObjectResult
    )

    fun saves3(file: MultipartFile): FileInfo{
        val result = amazons3.putObject(awsProperties.bucket, file.originalFilename, file.inputStream, ObjectMetadata())
        val url = "${awsProperties.endpoint}/${awsProperties.bucket}/${file.originalFilename}"
        return FileInfo(file.originalFilename!!, url, result)
    }

    @Transactional(rollbackOn = [IOException::class, RuntimeException::class])
    fun save(file: MultipartFile, parentFolderId: String): CreatedFileDTO{
        val fileInputStream = file.inputStream as FileInputStream
        val filename = Optional.ofNullable(file.originalFilename)
            .orElseThrow { IllegalStateException("Can no save file because the filename must not be null") }

        val newFile = File(
            filename = filename,
            size = file.size,
            originalFilename = filename,
            contentType = file.contentType
        )

        val createdFile = fileRepository.save(newFile)

        folderService.findById(parentFolderId)
            .orElseThrow { NotFoundException("Can not save file $filename because folder $parentFolderId does ot exists") }
            .apply { files.add(createdFile) }
            .let {
                val finalFilename = "${it.originalFolderName}${newFile.originalFilename}"
                DiskUtil.saveFileToDisk(fileInputStream, finalFilename)
            }

        return CreatedFileDTO(
            createdFile.id,
            createdFile.filename,
            createdFile.size,
            createdFile.contentType,
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