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

    @Async
    fun saveRootFolderForNewUser(user: User) {
        val rootFolder = Folder(
            name = "My unit",
            folderType = FolderType.ROOT,
            metadata = FileMetadata(ownerId = user.id!!, lastModifiedBy = user.id!!),
            visibility = FileVisibility.OWNER)

        folderRepository.save(rootFolder)
    }

    fun save(parentFolderId: String, request: FolderCreateRequest): FolderDTO {
        val parentFolder = this.findByInternal(parentFolderId)
        PermissionValidatorUtil.checkFolderPermissions(parentFolder, Permission.READ_WRITE)

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

    fun edit(id: String, request: FolderEditRequest): FolderDTO {
        val folder = this.findByInternal(id)
        PermissionValidatorUtil.checkFolderPermissions(folder, Permission.READ_WRITE)

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
        val userUnit = folderRepository.findByMetadataOwnerIdAndFolderType(authenticatedUser.id!!, FolderType.ROOT) ?:
            throw IllegalStateException("This user does not have an unit, how?!!!")

        return FolderMapper.toFolderDTO(userUnit, emptySet())
    }

    fun findById(id: String): FolderDTO {
        val folder = this.findByInternal(id)
        PermissionValidatorUtil.checkFolderPermissions(folder, Permission.READ_ONLY)

        return FolderMapper.toFolderDTO(folder, this.findFolderContributors(folder))
    }

    fun findAuthenticatedUserSharedFolders(): Collection<FolderDTO> {
        val authenticatedUser = SecurityUtil.getAuthenticatedUser()
        return folderRepository.findBySharedWithContains(authenticatedUser.id!!)
            .map { FolderMapper.toFolderDTO(it, this.findFolderContributors(it)) }
    }

    fun findSubFoldersByParentId(id: String): Set<FolderDTO> {
        val folder = this.findByInternal(id)
        PermissionValidatorUtil.checkFolderPermissions(folder, Permission.READ_ONLY)

        return folderRepository.findByIdIsIn(folder.subFolders)
            .filter { PermissionValidatorUtil.isFoldersOwner(it) || it.visibility != FileVisibility.OWNER }
            .map { FolderMapper.toFolderDTO(it, this.findFolderContributors(it)) }
            .toSet()
    }

    fun findFilesByFolderId(id: String): Collection<FileDTO> {
        val folder = this.findByInternal(id)
        return fileRepository.findByIdIsIn(folder.files)
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

        PermissionValidatorUtil.checkFolderPermissions(parentFolder, Permission.DELETE)
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