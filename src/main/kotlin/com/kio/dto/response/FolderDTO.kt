package com.kio.dto.response

import com.kio.entities.details.FolderDetails
import com.kio.entities.enums.FileVisibility

data class FolderDTO(
    val id: String,
    val ownerId: String,
    val name: String,
    val summary: FolderDetails,
    val createdAt: String,
    val lastModified: String,
    val contributors: Collection<ContributorDTO>,
    val visibility: FileVisibility
)
