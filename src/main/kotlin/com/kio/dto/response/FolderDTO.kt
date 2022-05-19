package com.kio.dto.response.find

data class FolderDTO(
    val id: String,
    val name: String,
    val color: String,
    val contributors: Collection<ContributorDTO>,
)
