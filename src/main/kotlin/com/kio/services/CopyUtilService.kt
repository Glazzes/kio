package com.kio.services

import com.kio.entities.details.FileMetadata
import com.kio.entities.File
import com.kio.entities.details.FileDetails
import com.kio.entities.Folder
import com.kio.entities.enums.FolderType
import com.kio.repositories.FolderRepository
import com.kio.shared.exception.FileTreeException
import com.kio.shared.exception.IllegalOperationException
import com.kio.shared.utils.FileUtils
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class CopyUtilService(
    private val folderRepository: FolderRepository
){

    fun cloneFile(file: File, parentFolder: Folder, bucketKey: String): File {
        return File(
            id = null,
            name = file.name,
            details = file.details,
            contentType = file.contentType,
            size = file.size,
            bucketKey = bucketKey,
            parentFolder = parentFolder.id!!,
            visibility = parentFolder.visibility,
            metadata = FileMetadata(parentFolder.metadata.ownerId)
        ).apply {
            metadata.createdAt = LocalDate.now()
            metadata.lastModifiedDate = LocalDateTime.now()
        }
    }

    fun cloneFolder(folder: Folder, parentFolder: Folder): Folder {
        return Folder(
            id = null,
            name = folder.name,
            folderType = folder.folderType,
            visibility = parentFolder.visibility,
            parentFolder = parentFolder.id,
            subFolders = folder.subFolders,
            files = folder.files,
            contributors = parentFolder.contributors,
            sharedWith = mutableSetOf(),
            metadata = FileMetadata(parentFolder.metadata.ownerId),
            summary = folder.summary
        ).apply {
            metadata.createdAt = LocalDate.now()
            metadata.lastModifiedDate = LocalDateTime.now()
        }
    }

    fun canCutFolder(source: Folder, destination: Folder) {
        if(source.id!! == destination.id!!) {
            throw IllegalOperationException("You can not cut a folder and paste it within itself")
        }

        if(destination.folderType == FolderType.ROOT) {
            throw IllegalOperationException("You can not cut folders to a unit folder")
        }

        val sourceContainsDestination = this.isDestinationWithinSource(source.subFolders,destination.id!!)
        if(sourceContainsDestination) {
            throw FileTreeException("""
            Destination makes part of source's tree branch, performing this operation will result in orphan folders    
            """.trimIndent())
        }
    }

    private fun isDestinationWithinSource(sourceSubFolders: Collection<String>, destinationId: String): Boolean {
        val sourceSubFolderIds = folderRepository.findSubFolderIdsByParentIds(sourceSubFolders)
            .flatMap { it.getSubFolders() }

        if(sourceSubFolderIds.contains(destinationId)) return true
        if(sourceSubFolderIds.isEmpty()) return false

        return isDestinationWithinSource(sourceSubFolderIds, destinationId)
    }

}