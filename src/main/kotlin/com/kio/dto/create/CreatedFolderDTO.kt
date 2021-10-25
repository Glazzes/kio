package com.kio.dto.create

import java.time.LocalDate

data class CreatedFolderDTO(
    val id: String?,
    val folderName: String,
    val size: Long,
    val lastModified: LocalDate?,
)