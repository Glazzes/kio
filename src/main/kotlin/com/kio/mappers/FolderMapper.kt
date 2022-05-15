package com.kio.mappers

import com.kio.dto.response.find.ContributorDTO
import com.kio.dto.response.find.FolderDTO
import com.kio.entities.Folder

object FolderMapper {

    fun toFolderDTO(folder: Folder, contributors: Set<ContributorDTO>): FolderDTO {
        return FolderDTO(
            id = folder.id!!,
            name = folder.name,
            color = folder.color,
            contributor = contributors)
    }

}