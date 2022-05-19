package com.kio.dto.response

import com.kio.entities.details.FileDetails
import java.time.LocalDate

data class FileDTO(
    val id: String,
    val name: String,
    val size: Long,
    val contentType: String,
    val details: FileDetails,
    val createdAt: LocalDate,
    val lastModified: LocalDate
)