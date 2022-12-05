package com.kio.services

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.DeleteObjectsRequest
import com.amazonaws.services.s3.model.ObjectMetadata
import com.kio.configuration.properties.BucketConfigurationProperties
import com.kio.dto.ModifyResourceRequest
import com.kio.dto.request.file.FileDeleteRequest
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

    fun save(
        request: FileUploadRequest,
        files: List<MultipartFile>,
        thumbnails: List<MultipartFile>?
    ): Collection<FileDTO> {
        val destination = this.findFolderById(request.to)
        val uploadSize = files.sumOf { it.size }

        PermissionValidatorUtil.verifyFolderPermissions(destination, Permission.READ_WRITE)
        val canUpload = spaceService.hasEnoughStorageToPerformOperation(destination, uploadSize)

        if(!canUpload) {
            throw InsufficientStorageException("The owner of this folder has ran out of storage space")
        }

        val thumbnailMap = thumbnails?.associateBy { it.originalFilename } ?: emptyMap()

        val filesToSave: MutableList<File> = ArrayList()
        val parentFolderNames = fileRepository.getFolderFilesNames(destination.files)

        for(file in files) {
            val bucketKey = "${destination.id}/${UUID.randomUUID()}-${UUID.randomUUID()}"
            this.saveS3Object(file, bucketKey)

            val validName = FileUtils.getValidName(file.originalFilename!!, parentFolderNames.map { it.getName() })

            val details = FileDetails(
                dimensions = request.details[file.originalFilename!!]?.dimensions,
                duration = request.details[file.originalFilename!!]?.duration
            )

            if(file.contentType!!.startsWith("audio")) {
                val audioSamples = MetadataUtil.getAudioSamples(file)
                details.audioSamples = audioSamples
            }

            if(file.contentType!!.startsWith("video") || file.contentType!!.endsWith("pdf")) {
                if(thumbnailMap.containsKey(file.originalFilename!!)) {
                    val key = "thumbnails/${bucketKey}"
                    this.saveS3Object(thumbnailMap[file.originalFilename!!]!!, key)
                    details.thumbnailKey = key
                }
            }

            if(file.contentType!!.endsWith("pdf")) {
                details.pages = MetadataUtil.getPdfPages(file)
            }

            val fileToSave = File(
                name = validName,
                details = details,
                contentType = file.contentType ?: "unknown",
                size = file.size,
                bucketKey = bucketKey,
                parentFolder = destination.id!!,
                visibility = destination.visibility,
                metadata = FileMetadata(destination.metadata.ownerId),
            )

            filesToSave.add(fileToSave)
        }

        val savedFiles = fileRepository.saveAll(filesToSave)

        destination.apply {
            this.files.addAll(savedFiles.map{ it.id!! })
            this.summary.files += filesToSave.size
            this.summary.size += uploadSize
        }

        folderRepository.save(destination)
        return savedFiles.map { FileMapper.toFileDTO(it) }
    }

    fun edit(request: ModifyResourceRequest): FileDTO {
        val parentFolder = this.findFolderById(request.from)

        if(!parentFolder.files.contains(request.resourceId)) {
            throw IllegalOperationException("This file ${request.resourceId} does not belong to folder ${request.from}")
        }

        PermissionValidatorUtil.verifyFolderPermissions(parentFolder, Permission.MODIFY)

        val file = this.findByIdInternal(request.resourceId).apply {
            name = request.name
            visibility = request.visibility
        }

        return FileMapper.toFileDTO(fileRepository.save(file))
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

        parentFolder.apply {
            this.files.removeAll(deleteRequest.files.toSet())
            this.summary.files -= filesToDelete.size
            this.summary.size -= filesToDeleteSize
        }

        folderRepository.save(parentFolder)

        s3.deleteObjects(deleteObjects)
        fileRepository.deleteAll(filesToDelete)
    }

    private fun saveS3Object(file: MultipartFile, key: String) {
        val s3ObjectMetadata = ObjectMetadata().apply {
            contentType = file.contentType ?: "unknown"
            contentLength = file.size
        }

        s3.putObject(bucketProperties.filesBucket, key, file.inputStream, s3ObjectMetadata)
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