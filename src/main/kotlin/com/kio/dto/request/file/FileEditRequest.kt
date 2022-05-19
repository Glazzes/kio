package com.kio.dto.request

import com.kio.entities.enums.FileVisibility

data class FileEditRequest(
    val name: String,
    val visibility: FileVisibility
)