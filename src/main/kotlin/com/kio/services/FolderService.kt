package com.kio.services

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.DeleteObjectRequest
import com.amazonaws.services.s3.model.DeleteObjectsRequest
import com.kio.dto.ContributorInfo
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
import com.kio.shared.utils.PermissionValidator
import com.kio.shared.utils.SecurityUtil
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class FolderService(
    private val folderRepository: FolderRepository,
    private val fileRepository: FileRepository,
    private val userRepository: UserRepository,
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
        PermissionValidator.checkFolderPermissions(parentFolder, Permission.READ_WRITE)

        val newFolder = Folder(
            name = name,
            visibility = parentFolder.visibility,
            contributors = parentFolder.contributors,
            metadata = AuditFileMetadata(parentFolder.metadata.ownerId))

        val savedFolder = folderRepository.save(newFolder)

        parentFolder.subFolders.add(newFolder.id!!)
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
        PermissionValidator.checkFolderPermissions(folder, Permission.READ_ONLY)

        return FolderMapper.toFolderDTO(folder, this.findFolderContributors(folder))
    }

    fun findSubFoldersByParentId(id: String): Set<FolderDTO> {
        val folder = this.findByInternal(id)
        PermissionValidator.checkFolderPermissions(folder, Permission.READ_ONLY)

        return folderRepository.findByIdIsIn(folder.subFolders)
            .filter { PermissionValidator.isFoldersOwner(it) || it.visibility != FileVisibility.OWNER }
            .map { FolderDTO(it.id!!, it.name, it.color, it.size, this.findFolderContributors(it)) }
            .toSet()
    }

    fun findFilesByFolderId(id: String): Collection<FileDTO> {
        val folder = this.findByInternal(id)
        return fileRepository.findByIdIsIn(folder.files)
            .map { FileDTO(it.id!!, it.name, it.size, it.contentType) }
    }

    private fun findFolderContributors(folder: Folder): Set<ContributorInfo> {
        val contributors = userRepository.findByIdIn(folder.contributors.keys)
        return contributors.map { ContributorInfo(it.id, it.username, it.profilePicture.url) }
            .toSet()
    }

    fun findFolderSizeById(id: String): Long {
        val parentFolder = this.findByInternal(id)
        val size = calculateSizeRecursively(parentFolder.subFolders)
        return size + parentFolder.size
    }

    private fun calculateSizeRecursively(folderIds: Collection<String>): Long {
        val subFolders = folderRepository.findByIdIsIn(folderIds)
        val subFoldersSize = subFolders.sumOf { it.size }
        val subFolderIds = subFolders.map { it.subFolders }.flatten()

        if(subFolderIds.isNotEmpty()) {
            return subFoldersSize + calculateSizeRecursively(subFolderIds)
        }

        return subFoldersSize
    }

    fun modifyState(id: String, state: FileVisibility) {
        val folder = this.findByInternal(id)
        folder.visibility = state
        folderRepository.save(folder)
    }

    fun deleteById(id: String) {
        val exists = folderRepository.existsById(id)
        if(!exists) {
            throw NotFoundException("You can no delete a file that does not exists.")
        }

        val folder = this.findByInternal(id)
        PermissionValidator.checkFolderPermissions(folder, Permission.DELETE)
        if(folder.folderType == FolderType.ROOT) {
            throw IllegalOperationException("You can not delete your unit")
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