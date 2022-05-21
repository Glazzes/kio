package com.kio.services

import com.kio.entities.Folder
import com.kio.entities.enums.FolderType
import com.kio.repositories.FolderRepository
import com.kio.repositories.UserRepository
import com.kio.shared.exception.InsufficientStorageException
import org.springframework.stereotype.Service

@Service
class SpaceService(
    private val userRepository: UserRepository,
    private val folderRepository: FolderRepository
){

    fun canUpload(folder: Folder, uploadSize: Long) {
        val folderOwner = userRepository.findById(folder.metadata.ownerId).get()
        val userUnitFolder = folderRepository.findByMetadataOwnerIdAndFolderType(folderOwner.id!!, FolderType.ROOT)

        val totalSize = uploadSize + this.calculateFolderSize(folder)
        if(totalSize > folderOwner.plan.space) {
            throw InsufficientStorageException("Performing this operation will exceed user's unit capacity")
        }
    }

    fun calculateFolderSize(folder: Folder): Long {
        val size = calculateSizeRecursively(folder.subFolders)
        return size + folder.summary.size
    }

    private fun calculateSizeRecursively(folderIds: Collection<String>): Long {
        val subFolders = folderRepository.findByIdIsIn(folderIds)
        val subFoldersSize = subFolders.sumOf { it.summary.size }
        val subFolderIds = subFolders.map { it.subFolders }.flatten()

        if(subFolderIds.isNotEmpty()) {
            return subFoldersSize + calculateSizeRecursively(subFolderIds)
        }

        return subFoldersSize
    }

}