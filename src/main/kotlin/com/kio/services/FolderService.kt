package com.kio.services

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.DeleteObjectsRequest
import com.kio.dto.response.find.ContributorDTO
import com.kio.dto.response.find.FileDTO
import com.kio.dto.response.save.SavedFolderDTO
import com.kio.dto.response.find.FolderDTO
import com.kio.entities.AuditFileMetadata
import com.kio.entities.Folder
import com.kio.entities.User
import com.kio.entities.enums.FileVisibility
import com.kio.entities.enums.FolderType
import com.kio.entities.enums.Permission
import com.kio.mappers.FolderMapper
import com.kio.repositories.FileRepository
import com.kio.repositories.FolderRepository
import com.kio.repositories.UserRepository
import com.kio.shared.exception.IllegalOperationException
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
            metadata = AuditFileMetadata(ownerId = user.id!!, lastModifiedBy = user.id!!),
            visibility = FileVisibility.OWNER)

        folderRepository.save(rootFolder)
    }

    fun save(parentFolderId: String, name: String): SavedFolderDTO {
        val parentFolder = this.findByInternal(parentFolderId)
        val subFolderNames = folderRepository.findFolderNamesByParentId(parentFolderId)
            .map { it.getName() }

        PermissionValidatorUtil.checkFolderPermissions(parentFolder, Permission.READ_WRITE)

        val newFolder = Folder(
            name = FileUtils.getValidName(name, subFolderNames),
            visibility = parentFolder.visibility,
            contributors = parentFolder.contributors,
            parentFolder = parentFolderId,
            metadata = AuditFileMetadata(parentFolder.metadata.ownerId)
        )

        val savedFolder = folderRepository.save(newFolder)

        parentFolder.subFolders.add(savedFolder.id!!)
        folderRepository.save(parentFolder)

        return SavedFolderDTO(savedFolder.id!!, savedFolder.name, savedFolder.metadata.createdAt!!)
    }

    fun findCurrentUserUnit(): FolderDTO {
        val authenticatedUser = SecurityUtil.getAuthenticatedUser()
        val userUnit = folderRepository.findByMetadataOwnerIdAndFolderType(authenticatedUser.id!!, FolderType.ROOT) ?:
            throw IllegalStateException("This user does not have an unit...")

        return FolderMapper.toFolderDTO(userUnit, emptySet())
    }

    fun findById(id: String): FolderDTO {
        val folder = this.findByInternal(id)
        PermissionValidatorUtil.checkFolderPermissions(folder, Permission.READ_ONLY)

        return FolderMapper.toFolderDTO(folder, this.findFolderContributors(folder))
    }

    fun findSubFoldersByParentId(id: String): Set<FolderDTO> {
        val folder = this.findByInternal(id)
        PermissionValidatorUtil.checkFolderPermissions(folder, Permission.READ_ONLY)

        return folderRepository.findByIdIsIn(folder.subFolders)
            .filter { PermissionValidatorUtil.isFoldersOwner(it) || it.visibility != FileVisibility.OWNER }
            .map { FolderDTO(it.id!!, it.name, it.color, this.findFolderContributors(it)) }
            .toSet()
    }

    fun findFilesByFolderId(id: String): Collection<FileDTO> {
        val folder = this.findByInternal(id)
        return fileRepository.findByIdIsIn(folder.files)
            .map { FileDTO(it.id!!, it.name, it.size, it.contentType) }
    }

    private fun findFolderContributors(folder: Folder): Set<ContributorDTO> {
        val contributors = userRepository.findByIdIn(folder.contributors.keys)
        return contributors.map { ContributorDTO(it.id, it.username, it.profilePicture.url) }
            .toSet()
    }

    fun findFolderSizeById(id: String): Long {
        val folder = this.findByInternal(id)
       return spaceService.calculateFolderSize(folder)
    }

    fun modifyState(id: String, state: FileVisibility) {
        val folder = this.findByInternal(id)
        folder.visibility = state
        folderRepository.save(folder)
    }

    fun deleteById(id: String) {
        val exists = folderRepository.existsById(id)
        if(!exists) {
            throw NotFoundException("You can no delete a folder that does not exists.")
        }

        val folder = this.findByInternal(id)
        PermissionValidatorUtil.checkFolderPermissions(folder, Permission.DELETE)
        if(folder.folderType == FolderType.ROOT) {
            throw IllegalOperationException("You can not delete your a unit folder")
        }

        folderRepository.delete(folder)
        fileRepository.deleteAllById(folder.files)
        deleteSubFoldersAndFiles(folder.subFolders)
    }

    private fun deleteSubFoldersAndFiles(subFolderIds: Collection<String>) {
        val subFolders = folderRepository.findByIdIsIn(subFolderIds)
        val foldersToDelete = subFolders.map { it.subFolders }.flatten()

        val fileIds = subFolders.map { it.files }.flatten()
        val filesToDelete = fileRepository.findByIdIsIn(fileIds)

        folderRepository.deleteAll(subFolders)
        fileRepository.deleteAll(filesToDelete)

        val deleteObjectsRequest = DeleteObjectsRequest("files.kio.com")
        deleteObjectsRequest.keys = filesToDelete.map { DeleteObjectsRequest.KeyVersion(it.bucketKey) }
        s3.deleteObjects(deleteObjectsRequest)

        if(foldersToDelete.isNotEmpty()) {
            deleteSubFoldersAndFiles(foldersToDelete)
        }
    }

    private fun findByInternal(id: String): Folder {
        return folderRepository.findById(id)
            .orElseThrow { NotFoundException("We could not found folder $id") }
    }

}