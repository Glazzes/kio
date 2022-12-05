package com.kio.dto

import com.kio.entities.enums.FileVisibility

data class ModifyResourceRequest(
    val from: String,
    val resourceId: String,
    val name: String,
    val visibility: FileVisibility
)