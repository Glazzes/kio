package com.kio.mappers

import com.kio.dto.ContributorInfo
import com.kio.dto.response.find.FolderDTO
import com.kio.entities.Folder

object FolderMapper {

    fun toFolderDTO(folder: Folder, contributors: Set<ContributorInfo>): FolderDTO {
        return FolderDTO(
            id = folder.id!!,
            name = folder.name,
            color = folder.color,
            size = folder.size,
            contributor = contributors)
    }

}