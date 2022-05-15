package com.kio.services

import com.kio.entities.Folder
import com.kio.entities.enums.FolderType
import com.kio.repositories.FolderRepository
import org.springframework.stereotype.Service

@Service
class SpaceService(
    private val folderRepository: FolderRepository
){

    fun checkUnitSize(folder: Folder) {
        val userUnitFolder = folderRepository.findByMetadataOwnerIdAndFolderType(
            folder.metadata.ownerId,
            FolderType.ROOT
        )

        val unitSize = this.calculateFolderSize(folder)
    }

    fun calculateFolderSize(folder: Folder): Long {
        val size = calculateSizeRecursively(folder.subFolders)
        return size + folder.size
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

}