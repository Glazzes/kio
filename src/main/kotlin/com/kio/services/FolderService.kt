package com.kio.services

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.DeleteObjectRequest
import com.amazonaws.services.s3.model.DeleteObjectsRequest
import com.kio.configuration.properties.BucketConfigurationProperties
import com.kio.dto.ModifyResourceRequest
import com.kio.dto.request.FavoriteRequest
import com.kio.dto.response.ContributorDTO
import com.kio.dto.response.FileDTO
import com.kio.dto.response.FolderDTO
import com.kio.dto.response.UnitSizeDTO
import com.kio.entities.details.FileMetadata
import com.kio.entities.Folder
import com.kio.entities.enums.FileVisibility
import com.kio.entities.enums.FolderType
import com.kio.entities.enums.Permission
import com.kio.mappers.FileMapper
import com.kio.mappers.FolderMapper
import com.kio.repositories.FileRepository
import com.kio.repositories.FolderRepository
import com.kio.repositories.UserRepository
import com.kio.shared.exception.NotFoundException
import com.kio.shared.utils.PermissionValidatorUtil
import com.kio.shared.utils.SecurityUtil
import com.kio.valueobjects.DeleteContent
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class FolderService(
    private val folderRepository: FolderRepository,
    private val fileRepository: FileRepository,
    private val spaceService: SpaceService,
    private val bucketProperties: BucketConfigurationProperties,
    private val s3: AmazonS3
){

    @Value("\${kio.files.page-size}")
    private var defaultPageSize: Int? = null

    fun save(parentFolderId: String, name: String): FolderDTO {
        val parentFolder = this.findByInternal(parentFolderId)

        PermissionValidatorUtil.verifyFolderPermissions(parentFolder, Permission.READ_WRITE)

        val newFolder = Folder(
            name = name,
            visibility = parentFolder.visibility,
            contributors = parentFolder.contributors,
            parentFolder = parentFolderId,
            metadata = FileMetadata(parentFolder.metadata.ownerId)
        )

        val savedFolder = folderRepository.save(newFolder)

        folderRepository.save(parentFolder.apply {
            this.subFolders.add(savedFolder.id!!)
            this.summary.folders++
        })

        return FolderMapper.toFolderDTO(newFolder)
    }

    fun findFavorites(): Collection<FolderDTO> {
        val authenticatedUser = SecurityUtil.getAuthenticatedUser()
        val pageRequest = PageRequest.of(0, 20, Sort.Direction.DESC, "metadata.lastModifiedDate")

        return folderRepository.findByFavoritesContains(authenticatedUser.id!!, pageRequest)
            .map { FolderMapper.toFolderDTO(it) }
            .toSet()
    }

    fun favorite(request: FavoriteRequest) {
        val authenticatedUser = SecurityUtil.getAuthenticatedUser()
        val folder = this.findByInternal(request.resourceId)
        PermissionValidatorUtil.verifyFolderPermissions(folder, Permission.READ_ONLY)

        folder.apply {
            if (request.favorite) {
                favorites.add(authenticatedUser.id!!)
            }else{
                favorites.remove(authenticatedUser.id!!)
            }
        }

        folderRepository.save(folder)
    }

    fun findAuthenticatedUserUnit(): FolderDTO {
        val authenticatedUser = SecurityUtil.getAuthenticatedUser()
        val userUnit = folderRepository.findByMetadataOwnerIdAndFolderType(authenticatedUser.id!!, FolderType.ROOT)

        if(userUnit == null) {
            val rootFolder = this.createRootFolder()
            return FolderMapper.toFolderDTO(rootFolder)
        }

        return FolderMapper.toFolderDTO(userUnit)
    }

    private fun createRootFolder(): Folder {
        val user = SecurityUtil.getAuthenticatedUser()

        val rootFolder = Folder(
            name = "My unit",
            folderType = FolderType.ROOT,
            metadata = FileMetadata(ownerId = user.id!!, lastModifiedBy = user.id!!),
            visibility = FileVisibility.OWNER)

        return folderRepository.save(rootFolder)
    }

    fun findById(id: String): FolderDTO {
        val folder = this.findByInternal(id)
        PermissionValidatorUtil.verifyFolderPermissions(folder, Permission.READ_ONLY)

        return FolderMapper.toFolderDTO(folder)
    }

    fun findAuthenticatedUserSharedFolders(): Collection<FolderDTO> {
        val authenticatedUser = SecurityUtil.getAuthenticatedUser()
        return folderRepository.findBySharedWithContains(authenticatedUser.id!!)
            .map { FolderMapper.toFolderDTO(it) }
    }

    fun findSubFoldersByParentId(id: String, page: Int): Page<FolderDTO> {
        val folder = this.findByInternal(id)
        PermissionValidatorUtil.verifyFolderPermissions(folder, Permission.READ_ONLY)

        val pageRequest = PageRequest.of(
            page,
            defaultPageSize!!,
            Sort.by(Sort.Direction.DESC, "metadata.lastModifiedDate")
        )

        val folderPage = folderRepository.findByIdIsIn(folder.subFolders, pageRequest)
        folderPage.removeAll { !PermissionValidatorUtil.verifyFolderPermissionsAsBoolean(it, Permission.READ_ONLY) }

        return folderPage.map { FolderMapper.toFolderDTO(it) }
    }

    fun edit(request: ModifyResourceRequest): FolderDTO {
        val folder = this.findByInternal(request.resourceId)
        PermissionValidatorUtil.verifyFolderPermissions(folder, Permission.MODIFY)

        if(folder.visibility !== request.visibility) {
            this.editSubFoldersAndFiles(folder, request.visibility)
        }

        val savedFolder = folderRepository.save(folder.apply {
            visibility = request.visibility
            name = request.name
        })
        return FolderMapper.toFolderDTO(savedFolder)
    }

    private fun editSubFoldersAndFiles(folder: Folder, visibility: FileVisibility) {
        val subFolders = folderRepository.findByIdIsIn(folder.subFolders).map {
            it.visibility = visibility
            it
        }

        val files = fileRepository.findByIdIsIn(folder.files).map {
            it.visibility = visibility
            it
        }

        folderRepository.saveAll(subFolders)
        fileRepository.saveAll(files)

        for(subFolder in subFolders) {
            this.editSubFoldersAndFiles(subFolder, visibility)
        }

    }

    fun findFilesByFolderId(id: String, page: Int): Page<FileDTO> {
        val folder = this.findByInternal(id)
        val pageRequest = PageRequest.of(
            page,
            defaultPageSize!!,
            Sort.by(Sort.Direction.DESC, "metadata.lastModifiedDate")
        )

        return fileRepository.findByIdIsIn(folder.files, pageRequest)
            .map { FileMapper.toFileDTO(it) }
    }

    fun findUnitSize(): UnitSizeDTO {
        val authenticatedUser = SecurityUtil.getAuthenticatedUser()
        val userUnit = folderRepository.findByMetadataOwnerIdAndFolderType(authenticatedUser.id!!, FolderType.ROOT) ?:
            throw NotFoundException("User with id ${authenticatedUser.id} has no unit, how?!!!")

        val used = spaceService.calculateFolderSize(userUnit)
        return UnitSizeDTO(used, authenticatedUser.plan.space)
    }

    fun findFolderSizeById(id: String): Long {
        val folder = this.findByInternal(id)
        return spaceService.calculateFolderSize(folder)
    }

    fun deleteFolder(id: String) {
        val folderToDelete = this.findByInternal(id)
        PermissionValidatorUtil.verifyFolderPermissions(folderToDelete, Permission.DELETE)

        val parentFolder = this.findByInternal(folderToDelete.parentFolder!!)
        val contentToDelete = this.deleteContents(folderToDelete)
        contentToDelete.folders.add(folderToDelete)

        if(contentToDelete.fileIds.isNotEmpty()) {
            val filesToDelete = fileRepository.findAllById(contentToDelete.fileIds)
            val deleteObjets = DeleteObjectsRequest(bucketProperties.filesBucket).apply {
                keys = filesToDelete.map { DeleteObjectsRequest.KeyVersion(it.bucketKey) }
            }

            s3.deleteObjects(deleteObjets)
            fileRepository.deleteAll(filesToDelete)
        }

        parentFolder.apply {
            summary.folders = summary.folders - 1
            subFolders.remove(folderToDelete.id)
        }

        folderRepository.deleteAll(contentToDelete.folders)
        folderRepository.save(parentFolder)
    }

    private fun deleteContents(parentFolder: Folder): DeleteContent {
        val deleteContent = DeleteContent()

        if(parentFolder.files.isNotEmpty()) {

            deleteContent.apply {
                fileIds.addAll(parentFolder.files)
            }
        }

        if(parentFolder.subFolders.isEmpty()) {
            return deleteContent
        }

        val subFolders = folderRepository.findByIdIsIn(parentFolder.subFolders)
        for (sub in subFolders) {
            val subDeleteContent = this.deleteContents(sub)
            deleteContent.folders.add(sub)
            deleteContent.folders.addAll(subDeleteContent.folders)
            deleteContent.fileIds.addAll(subDeleteContent.fileIds)
        }

        return deleteContent
    }

    fun deleteContentsRecursively(folder: Folder, container: MutableCollection<Folder> = mutableSetOf()): MutableCollection<Folder> {
        val subFolders = folderRepository.findByIdIsIn(folder.subFolders)
        container.add(folder)

        if(folder.files.isNotEmpty()) {
            val files = fileRepository.findByIdIsIn(folder.files)
            val deleteObjectsRequest = DeleteObjectsRequest(bucketProperties.filesBucket).apply {
                keys = files.map { DeleteObjectsRequest.KeyVersion(it.bucketKey) }
            }

            s3.deleteObjects(deleteObjectsRequest)
            fileRepository.deleteAll(files)
        }

        for (sub in subFolders) {
            this.deleteContentsRecursively(sub, container)
        }

        return container
    }

    private fun findByInternal(id: String): Folder {
        return folderRepository.findById(id)
            .orElseThrow { NotFoundException("We could not found folder $id") }
    }

}