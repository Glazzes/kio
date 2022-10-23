package com.kio.services

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.DeleteObjectsRequest
import com.kio.dto.request.folder.FolderCreateRequest
import com.kio.dto.request.folder.FolderEditRequest
import com.kio.dto.response.ContributorDTO
import com.kio.dto.response.FileDTO
import com.kio.dto.response.FolderDTO
import com.kio.entities.details.FileMetadata
import com.kio.entities.Folder
import com.kio.entities.User
import com.kio.entities.enums.FileVisibility
import com.kio.entities.enums.FolderType
import com.kio.entities.enums.Permission
import com.kio.mappers.FileMapper
import com.kio.mappers.FolderMapper
import com.kio.repositories.FileRepository
import com.kio.repositories.FolderRepository
import com.kio.repositories.UserRepository
import com.kio.shared.enums.FolderCreationStrategy
import com.kio.shared.exception.AlreadyExistsException
import com.kio.shared.exception.NotFoundException
import com.kio.shared.utils.FileUtils
import com.kio.shared.utils.PermissionValidatorUtil
import com.kio.shared.utils.SecurityUtil
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class FolderService(
    private val folderRepository: FolderRepository,
    private val fileRepository: FileRepository,
    private val userRepository: UserRepository,
    private val spaceService: SpaceService,
    private val s3: AmazonS3
){

    private val defaultPageSize: Int = 20

    fun save(parentFolderId: String, request: FolderCreateRequest): FolderDTO {
        val parentFolder = this.findByInternal(parentFolderId)
        PermissionValidatorUtil.verifyFolderPermissions(parentFolder, Permission.READ_WRITE)

        val subFolderNames = folderRepository.findFolderNamesByParentId(parentFolderId)
            .map { it.getName() }

        if(subFolderNames.contains(request.name) && request.strategy == FolderCreationStrategy.OMIT) {
            throw AlreadyExistsException("A folder with name ${request.name} already exists within folder $parentFolderId")
        }

        val newFolder = Folder(
            name = FileUtils.getValidName(request.name, subFolderNames),
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

        return FolderMapper.toFolderDTO(newFolder, emptyList())
    }

    fun findFavorites(): Collection<FolderDTO> {
        val authenticatedUser = SecurityUtil.getAuthenticatedUser()
        val pageRequest = PageRequest.of(0, 20, Sort.Direction.DESC, "metadata.lastModifiedDate")

        return folderRepository.findByMetadataOwnerIdAndIsFavorite(authenticatedUser.id!!, true, pageRequest)
            .map { FolderMapper.toFolderDTO(it, this.findFolderContributors(it)) }
            .toSet()
    }

    fun fave(folderIds: Collection<String>) {
        val folders = folderRepository.findByIdIsIn(folderIds)
            .filter { PermissionValidatorUtil.isResourceOwner(it) }
            .map { it.apply { this.isFavorite = true } }

        folderRepository.saveAll(folders)
    }

    fun edit(id: String, request: FolderEditRequest): FolderDTO {
        val folder = this.findByInternal(id)
        PermissionValidatorUtil.verifyFolderPermissions(folder, Permission.READ_WRITE)

        val folderNames = folderRepository.findFolderNamesByParentId(folder.id!!)
            .map { it.getName() }

        folder.apply {
            this.color = request.color
            this.name = FileUtils.getValidName(request.name, folderNames)
            this.visibility = request.visibility
        }

        val editedFolder = folderRepository.save(folder)
        return FolderMapper.toFolderDTO(editedFolder, this.findFolderContributors(editedFolder))
    }

    fun findAuthenticatedUserUnit(): FolderDTO {
        val authenticatedUser = SecurityUtil.getAuthenticatedUser()
        val userUnit = folderRepository.findByMetadataOwnerIdAndFolderType(authenticatedUser.id!!, FolderType.ROOT)

        if(userUnit == null) {
            val rootFolder = this.createRootFolder()
            return FolderMapper.toFolderDTO(rootFolder, emptySet())
        }

        return FolderMapper.toFolderDTO(userUnit, emptySet())
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

        return FolderMapper.toFolderDTO(folder, this.findFolderContributors(folder))
    }

    fun findAuthenticatedUserSharedFolders(): Collection<FolderDTO> {
        val authenticatedUser = SecurityUtil.getAuthenticatedUser()
        return folderRepository.findBySharedWithContains(authenticatedUser.id!!)
            .map { FolderMapper.toFolderDTO(it, this.findFolderContributors(it)) }
    }

    fun findSubFoldersByParentId(id: String, page: Int): Page<FolderDTO> {
        val folder = this.findByInternal(id)
        PermissionValidatorUtil.verifyFolderPermissions(folder, Permission.READ_ONLY)

        val pageRequest = PageRequest.of(
            page,
            defaultPageSize,
            Sort.by(Sort.Direction.DESC, "metadata.lastModifiedDate")
        )

        val page = folderRepository.findByIdIsIn(folder.subFolders, pageRequest)
        page.removeAll { !PermissionValidatorUtil.verifyFolderPermissionsAsBoolean(it, Permission.READ_ONLY) }

        return page.map { FolderMapper.toFolderDTO(it, this.findFolderContributors(it)) }
    }

    fun findFilesByFolderId(id: String, page: Int): Page<FileDTO> {
        val folder = this.findByInternal(id)
        val pageRequest = PageRequest.of(
            page,
            defaultPageSize,
            Sort.by(Sort.Direction.DESC, "metadata.lastModifiedDate")
        )

        return fileRepository.findByIdIsIn(folder.files, pageRequest)
            .map { FileMapper.toFileDTO(it) }
    }

    private fun findFolderContributors(folder: Folder): Collection<ContributorDTO> {
        val contributors = userRepository.findByIdIn(folder.contributors.keys)
        return contributors.map { ContributorDTO(it.id, it.username, it.profilePictureId) }
            .toSet()
    }

    fun findFolderSizeById(id: String): Long {
        val folder = this.findByInternal(id)
       return spaceService.calculateFolderSize(folder)
    }

    fun deleteAll(from: String, subFoldersIds: Collection<String>) {
        val parentFolder = this.findByInternal(from)

        if(!parentFolder.subFolders.containsAll(subFoldersIds)) {
            throw NotFoundException("At least one of the folders to delete does not belong to its parent")
        }

        PermissionValidatorUtil.verifyFolderPermissions(parentFolder, Permission.DELETE)
        val subFolders = folderRepository.findByIdIsIn(subFoldersIds)
        for(sub in subFolders) {
            this.deleteSubFoldersAndFiles(sub.subFolders)
        }

        folderRepository.save(parentFolder.apply {
            this.subFolders.removeAll(subFoldersIds.toSet())
            this.summary.folders -= subFolders.size
        })

        folderRepository.deleteAll(subFolders)
    }

    private fun deleteSubFoldersAndFiles(subFolderIds: Collection<String>) {
        val subFolders = folderRepository.findByIdIsIn(subFolderIds)
        val foldersToDelete = subFolders.map { it.subFolders }.flatten()

        val fileIds = subFolders.map { it.files }.flatten()
        val filesToDelete = fileRepository.findByIdIsIn(fileIds)

        folderRepository.deleteAll(subFolders)
        fileRepository.deleteAll(filesToDelete)

        if(filesToDelete.isNotEmpty()) {
            val deleteObjectsRequest = DeleteObjectsRequest("files.kio.com")
            deleteObjectsRequest.keys = filesToDelete.map { DeleteObjectsRequest.KeyVersion(it.bucketKey) }
            s3.deleteObjects(deleteObjectsRequest)

            deleteSubFoldersAndFiles(foldersToDelete)
        }
    }

    private fun findByInternal(id: String): Folder {
        return folderRepository.findById(id)
            .orElseThrow { NotFoundException("We could not found folder $id") }
    }

}