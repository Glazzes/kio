package com.kio.dto.response

import com.kio.entities.details.FolderDetails

data class FolderDTO(
    val id: String,
    val name: String,
    val color: String,
    val summary: FolderDetails,
    val contributors: Collection<ContributorDTO>,
)
