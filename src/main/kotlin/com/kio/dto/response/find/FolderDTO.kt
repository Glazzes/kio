package com.kio.dto.response.find

import com.kio.dto.Contributor
import com.kio.dto.SubFolder

data class FolderDTO(
    val id: String,
    val name: String,
    val color: String,
    val size: Long,
    val contributor: Set<Contributor>,
    val subFolders: Set<SubFolder>,
    val files: Set<FileDTO>
)
