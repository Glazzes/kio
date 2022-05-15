package com.kio.shared.utils

import com.kio.entities.AuditFileMetadata
import com.kio.entities.File
import com.kio.entities.Folder
import com.kio.entities.enums.FolderType
import com.kio.shared.exception.IllegalOperationException

object CopyUtil {

    fun cloneFile(file: File, parentFolder: Folder, newName: String, bucketKey: String): File {
        return File(
            id = null,
            name = newName,
            contentType = file.contentType,
            size = file.size,
            bucketKey = bucketKey,
            parentFolder = parentFolder.id!!,
            visibility = parentFolder.visibility,
            metadata = AuditFileMetadata(parentFolder.metadata.ownerId)
        )
    }

    fun canCut(source: Folder, destination: Folder) {
        if(source.id!! == destination.id!!) {
            throw IllegalOperationException("You can not cut a folder and paste it within itself")
        }

        if(source.folderType == FolderType.ROOT) {
            throw IllegalOperationException("You can not cut a unit folder")
        }
    }

}