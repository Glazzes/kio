package com.kio.services

import com.kio.entities.Folder
import com.kio.entities.enums.FolderType
import com.kio.repositories.FolderRepository
import com.kio.repositories.UserRepository
import com.kio.shared.exception.NotFoundException
import org.springframework.stereotype.Service

@Service
class SpaceService(
    private val userRepository: UserRepository,
    private val folderRepository: FolderRepository
){

    fun hasEnoughStorageToPerformOperation(affectedFolder: Folder, operationSize: Long): Boolean {
        val folderOwner = userRepository.findById(affectedFolder.metadata.ownerId).get()

        val userUnitFolder = folderRepository.findByMetadataOwnerIdAndFolderType(folderOwner.id!!, FolderType.ROOT)
            ?: throw NotFoundException("This user does not have a root folder, how is it?")

        val totalSize = operationSize + this.calculateFolderSize(userUnitFolder)
        return totalSize <= folderOwner.plan.space
    }

    fun calculateFolderSize(folder: Folder): Long {
        val subFolders = folderRepository.findByIdIsIn(folder.subFolders)
        var result: Long = 0
        for (sub in subFolders) {
            result += calculateFolderSize(sub)
        }

        return folder.summary.size + result
    }

}