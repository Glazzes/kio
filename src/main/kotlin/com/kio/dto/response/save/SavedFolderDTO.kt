package com.kio.dto.response.save

import java.time.LocalDate

data class SavedFolderDTO(
    val id: String,
    val name: String,
    val createdAt: LocalDate,
)