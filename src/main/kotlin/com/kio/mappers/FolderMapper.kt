package com.kio.mappers

import com.kio.dto.response.ContributorDTO
import com.kio.dto.response.FolderDTO
import com.kio.entities.Folder

object FolderMapper {

    fun toFolderDTO(folder: Folder, contributors: Collection<ContributorDTO>) = FolderDTO(
        id = folder.id!!,
        ownerId = folder.metadata.ownerId,
        name = folder.name,
        color = folder.color,
        createdAt = folder.metadata.createdAt!!,
        lastModified = folder.metadata.lastModifiedDate!!,
        summary = folder.summary,
        contributors = contributors
    )

}