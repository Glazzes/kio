package com.kio.dto.response

import com.kio.entities.details.FolderDetails

data class FolderDTO(
    val id: String,
    val ownerId: String,
    val name: String,
    val summary: FolderDetails,
    val createdAt: String,
    val lastModified: String,
    val contributors: Collection<ContributorDTO>,
)
