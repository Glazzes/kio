package com.kio.dto.response.find

import com.kio.dto.ContributorInfo

data class FolderDTO(
    val id: String,
    val name: String,
    val color: String,
    val size: Long,
    val contributor: Set<ContributorInfo>,
)
