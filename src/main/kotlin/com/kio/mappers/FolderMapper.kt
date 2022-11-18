package com.kio.mappers

import com.kio.dto.response.ContributorDTO
import com.kio.dto.response.FolderDTO
import com.kio.entities.Folder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object FolderMapper {

    private val formatter = DateTimeFormatter.ofPattern("dd MMMM, yyyy", Locale.ENGLISH)

    fun toFolderDTO(folder: Folder, contributors: Collection<ContributorDTO>) = FolderDTO(
        id = folder.id!!,
        ownerId = folder.metadata.ownerId,
        name = folder.name,
        color = folder.color,
        createdAt = folder.metadata.createdAt!!.format(formatter),
        lastModified = folder.metadata.lastModifiedDate!!.format(formatter),
        summary = folder.summary,
        contributors = contributors)

}