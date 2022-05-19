package com.kio.dto.request.folder

import com.kio.entities.enums.FileVisibility

data class FolderEditRequest(
    val name: String,
    val color: String,
    val visibility: FileVisibility
)