package com.kio.dto.request.folder

import com.kio.shared.enums.FolderCreationStrategy

data class FolderCreateRequest(
    val name: String,
    val strategy: FolderCreationStrategy
)