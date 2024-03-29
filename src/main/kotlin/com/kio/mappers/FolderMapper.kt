package com.kio.mappers

import com.kio.dto.response.ContributorDTO
import com.kio.dto.response.FolderDTO
import com.kio.entities.Folder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object FolderMapper {

    private val formatter = DateTimeFormatter.ofPattern("dd MMMM, yyyy", Locale.ENGLISH)

    fun toFolderDTO(folder: Folder) = FolderDTO(
        id = folder.id!!,
        ownerId = folder.metadata.ownerId,
        name = folder.name,
        createdAt = folder.metadata.createdAt!!.format(formatter),
        lastModified = folder.metadata.lastModifiedDate!!.format(formatter),
        summary = folder.summary,
        visibility = folder.visibility
    )

}