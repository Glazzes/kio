package com.kio.dto.response

import com.kio.entities.details.FolderDetails
import java.time.LocalDate

data class FolderDTO(
    val id: String,
    val ownerId: String,
    val name: String,
    val color: String,
    val summary: FolderDetails,
    val createdAt: LocalDate,
    val lastModified: LocalDate,
    val contributors: Collection<ContributorDTO>,
)
