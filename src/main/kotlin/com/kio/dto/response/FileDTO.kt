package com.kio.dto.response

import com.kio.entities.details.FileDetails
import java.time.LocalDate
import java.time.LocalDateTime

data class FileDTO(
    val id: String,
    val ownerId: String,
    val name: String,
    val size: Long,
    val contentType: String,
    val details: FileDetails,
    val createdAt: String,
    val lastModified: String
)