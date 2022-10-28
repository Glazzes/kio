package com.kio.services

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.DeleteObjectsRequest
import com.amazonaws.services.s3.model.ObjectMetadata
import com.kio.configuration.properties.BucketConfigurationProperties
import com.kio.dto.request.file.FileDeleteRequest
import com.kio.dto.request.file.FileEditRequest
import com.kio.dto.request.file.FileUploadRequest
import com.kio.dto.response.FileDTO
import com.kio.entities.File
import com.kio.entities.details.FileMetadata
import com.kio.entities.Folder
import com.kio.entities.details.FileDetails
import com.kio.entities.enums.Permission
import com.kio.mappers.FileMapper
import com.kio.repositories.FileRepository
import com.kio.repositories.FolderRepository
import com.kio.shared.exception.IllegalOperationException
import com.kio.shared.exception.InsufficientStorageException
import com.kio.shared.exception.NotFoundException
import com.kio.shared.utils.FileUtils
import com.kio.shared.utils.MetadataUtil
import com.kio.shared.utils.PermissionValidatorUtil
import com.kio.shared.utils.SecurityUtil
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.*
import kotlin.collections.ArrayList

@Service
class FileService(
    private val fileRepository: FileRepository,
    private val folderRepository: FolderRepository,
    private val spaceService: SpaceService,
    private val bucketProperties: BucketConfigurationProperties,
    private val s3: AmazonS3
) {

    fun save(request: FileUploadRequest, files: List<MultipartFile>): Collection<FileDTO> {
        val folder = this.findFolderById(request.to)
        val filesSize = files.sumOf { it.size }

        PermissionValidatorUtil.verifyFolderPermissions(folder, Permission.READ_WRITE)
        val canUpload = spaceService.canUpload(folder, filesSize)
        if(!canUpload) {
            throw InsufficientStorageException("The owner of this folder has ran out of storage space")
        }

        val filesToSave: MutableList<File> = ArrayList()
        val parentFolderNames = fileRepository.getFolderFilesNames(folder.files)

        for(file in files) {
            val bucketKey = "${folder.id}/${UUID.randomUUID()}-${UUID.randomUUID()}"

            val s3Metadata = ObjectMetadata()
            s3Metadata.contentType = file.contentType
            s3Metadata.contentLength = file.size

            s3.putObject(bucketProperties.filesBucket, bucketKey, file.inputStream, s3Metadata)

            val validName = FileUtils.getValidName(file.originalFilename!!, parentFolderNames.map { it.getName() })

            var audioSamples: Array<Int>? = null
            if(file.contentType!!.startsWith("audio")) {
                audioSamples = MetadataUtil.getAudioSamples(file)
            }

            val fileToSave = File(
                name = validName,
                details = FileDetails(audioSamples = audioSamples),
                contentType = file.contentType ?: "UNKNOWN",
                size = file.size,
                bucketKey = bucketKey,
                parentFolder = folder.id!!,
                visibility = folder.visibility,
                metadata = FileMetadata(folder.metadata.ownerId),
            )

            filesToSave.add(fileToSave)
        }

        val savedFiles = fileRepository.saveAll(filesToSave)

        folderRepository.save(folder.apply {
            this.files.addAll(savedFiles.map{ it.id!! })
            this.summary.files += filesToSave.size
            this.summary.size += filesSize
        })

        return savedFiles.map { FileMapper.toFileDTO(it) }
    }

    fun findById(fileId: String): FileDTO {
        val file = this.findByIdInternal(fileId)
        val parentFolder = this.findFolderById(file.parentFolder)
        PermissionValidatorUtil.verifyFolderPermissions(parentFolder, Permission.READ_ONLY)

        return FileMapper.toFileDTO(file)
    }

    fun findFavorites(): Collection<FileDTO> {
        val authenticatedUser = SecurityUtil.getAuthenticatedUser()
        val pageRequest = PageRequest.of(0, 20, Sort.Direction.DESC, "metadata.lastModifiedDate")

        return fileRepository.findByMetadataOwnerIdAndIsFavorite(authenticatedUser.id!!, true, pageRequest)
            .map { FileMapper.toFileDTO(it) }
            .toSet()
    }

    fun fave(fileIds: Collection<String>) {
        val authenticatedUser = SecurityUtil.getAuthenticatedUser()
        val files = fileRepository.findByIdIsIn(fileIds)
            .filter { it.metadata.ownerId == authenticatedUser.id!! }
            .map { it.apply { this.isFavorite = true } }

        fileRepository.saveAll(files)
    }

    fun edit(id: String, request: FileEditRequest): FileDTO {
        val file = this.findByIdInternal(id)
        val parentFolder = this.findFolderById(file.parentFolder)

        PermissionValidatorUtil.verifyFolderPermissions(parentFolder, Permission.READ_WRITE)

        val parentFilenames = fileRepository.findFilesNamesByParentId(file.parentFolder)
            .map { it.getName() }

        file.apply {
            this.name = FileUtils.getValidName(request.name, parentFilenames)
            this.visibility = request.visibility
        }

        return FileMapper.toFileDTO(file)
    }

    fun deleteAll(deleteRequest: FileDeleteRequest) {
        val parentFolder = this.findFolderById(deleteRequest.from)

        if(!parentFolder.files.containsAll(deleteRequest.files)) {
            throw IllegalOperationException("At least one of the files does not belong to this folder ${deleteRequest.from}")
        }

        PermissionValidatorUtil.verifyFolderPermissions(parentFolder, Permission.READ_WRITE, Permission.DELETE)
        val filesToDelete = fileRepository.findByIdIsIn(deleteRequest.files)
        val filesToDeleteSize = filesToDelete.sumOf { it.size }

        val deleteObjects = DeleteObjectsRequest(bucketProperties.filesBucket).apply {
            keys = filesToDelete.map { DeleteObjectsRequest.KeyVersion(it.bucketKey) }
        }

        folderRepository.save(parentFolder.apply {
            this.files.removeAll(deleteRequest.files.toSet())
            this.summary.files -= filesToDelete.size
            this.summary.size -= filesToDeleteSize
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