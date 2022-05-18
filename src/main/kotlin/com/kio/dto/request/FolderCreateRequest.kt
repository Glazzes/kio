package com.kio.dto.request

import com.kio.shared.enums.FolderCreationStrategy

data class FolderCreateRequest(
    val name: String,
    val strategy: FolderCreationStrategy
)