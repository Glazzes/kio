package com.kio.services

import com.kio.dto.ContributorInfo
import com.kio.dto.response.save.SavedFolderDTO
import com.kio.dto.response.find.FolderDTO
import com.kio.entities.AuditFileMetadata
import com.kio.entities.Folder
import com.kio.entities.User
import com.kio.entities.enums.FileState
import com.kio.entities.enums.FolderType
import com.kio.entities.enums.Permission
import com.kio.mappers.FolderMapper
import com.kio.repositories.FolderRepository
import com.kio.repositories.UserRepository
import com.kio.shared.exception.NotFoundException
import com.kio.shared.utils.SecurityUtil
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class FolderService(
    private val folderRepository: FolderRepository,
    private val userRepository: UserRepository
){

    @Async
    fun saveRootFolderForNewUser(user: User) {
        val rootFolder = Folder(
            name = "My unit",
            folderType = FolderType.ROOT_UNIT,
            metadata = AuditFileMetadata(owner = user.id!!, lastModifiedBy = user.id!!),
            state = FileState.OWNER)

        folderRepository.save(rootFolder)
    }

    fun save(parentFolderId: String, name: String): SavedFolderDTO {
        val parentFolder = this.findByInternal(parentFolderId)
        PermissionValidator.checkResourcePermissions(parentFolder, Permission.CAN_READ)

        val newFolder = Folder(
            name = name,
            state = parentFolder.state,
            contributors = parentFolder.contributors,
            metadata = AuditFileMetadata(parentFolder.metadata.owner))

        val savedFolder = folderRepository.save(newFolder)

        parentFolder.subFolders.add(newFolder.id!!)
        folderRepository.save(parentFolder)

        return SavedFolderDTO(savedFolder.id!!, savedFolder.name, savedFolder.metadata.createdAt!!)
    }

    fun findCurrentUserUnit(): FolderDTO {
        val authenticatedUser = SecurityUtil.getAuthenticatedUser()
        val userUnit = folderRepository.findByMetadataOwnerAndFolderType(authenticatedUser.id!!, FolderType.ROOT_UNIT) ?:
            throw IllegalStateException("This user does not have an unit...")

        return FolderMapper.toFolderDTO(userUnit, this.getFolderContributors(userUnit))
    }

    fun findById(id: String): FolderDTO {
        val folder = this.findByInternal(id)
        PermissionValidator.checkResourcePermissions(folder, Permission.CAN_READ)

        return FolderMapper.toFolderDTO(folder, this.getFolderContributors(folder))
    }

    private fun getFolderContributors(folder: Folder): Set<ContributorInfo> {
        val contributors = userRepository.findByIdIn(folder.contributors.keys)
        return contributors.map { ContributorInfo(it.id, it.username, it.profilePicture.url) }
            .toSet()
    }

    fun findSubFoldersByParentId(id: String): Set<FolderDTO> {
        val folder = this.findByInternal(id)
        PermissionValidator.checkResourcePermissions(folder, Permission.CAN_READ)

        val subFolders = folderRepository.findByIdIn(folder.subFolders)
        return subFolders.filter { it.state === FileState.PUBLIC || it.state == FileState.RESTRICTED }
            .map { FolderDTO(folder.id!!, folder.name, folder.color, folder.size, this.getFolderContributors(it)) }
            .toSet()
    }

    fun modifyState(id: String, state: FileState) {
        val folder = this.findByInternal(id)
        folder.state = state
        folderRepository.save(folder)
    }

    private fun findByInternal(id: String): Folder {
        return folderRepository.findById(id)
            .orElseThrow { NotFoundException("We could not found folder $id") }
    }

}