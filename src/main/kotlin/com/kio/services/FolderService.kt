package com.kio.services

import com.kio.dto.response.save.SavedFolderDTO
import com.kio.dto.response.find.FolderDTO
import com.kio.entities.AuditFileMetadata
import com.kio.entities.Folder
import com.kio.entities.User
import com.kio.entities.enums.FileState
import com.kio.entities.enums.FolderType
import com.kio.entities.enums.Permission
import com.kio.repositories.FolderRepository
import com.kio.repositories.UserRepository
import com.kio.shared.exception.IllegalOperationException
import com.kio.shared.exception.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class FolderService(
    private val folderRepository: FolderRepository,
    private val userRepository: UserRepository
){

    fun saveRootFolderForNewUser(user: User) {
        val rootFolder = Folder(
            name = "My unit",
            folderType = FolderType.ROOT_UNIT,
            metadata = AuditFileMetadata(user.id!!),
            state = FileState.OWNER)

        folderRepository.save(rootFolder)
    }

    fun save(parentFolderId: String, name: String): SavedFolderDTO {
        val parentFolder = folderRepository.findById(parentFolderId)
            .orElseThrow { NotFoundException("You can not create a sub folder within a folder that does not exists") }

        val canCreateFolder = PermissionValidator.canPerformOperation(parentFolder, Permission.CAN_CREATE)
        if(!canCreateFolder) throw IllegalOperationException("You are not allowed to create a folder within this folder")

        val newFolder = Folder(
            name = name,
            state = parentFolder.state,
            coowners = parentFolder.coowners,
            metadata = AuditFileMetadata(parentFolder.metadata.owner)
        )

        val savedFolder = folderRepository.save(newFolder)
        parentFolder.subFolders.add(newFolder.id!!)
        return SavedFolderDTO(savedFolder.id!!, savedFolder.name, savedFolder.metadata.createdAt!!)
    }

    fun findById(id: String): FolderDTO {
        val folder = folderRepository.findById(id)
            .orElseThrow { NotFoundException("We could not found folder $id") }

        val canReadFolder = PermissionValidator.canPerformOperation(folder, Permission.CAN_READ)
        if(!canReadFolder) throw IllegalOperationException("You are not allowed to read this folder")
    }

    fun findByIdInternal(id: String): Folder {
        return folderRepository.findById(id)
            .orElseThrow { NotFoundException("We could not found folder $id") }
    }

}