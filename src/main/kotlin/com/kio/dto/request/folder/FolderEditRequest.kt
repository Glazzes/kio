package com.kio.dto.request.folder

import com.kio.entities.enums.FileVisibility

data class FolderEditRequest(
    val name: String,
    val visibility: FileVisibility
)