package com.kio.dto.response.save

import java.time.LocalDate

data class SavedFileDTO(
    val id: String?,
    val name: String,
    val size: Long,
    val contentType: String,
    val createdAt: LocalDate
)