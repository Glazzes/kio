package com.kio.dto.response

import com.kio.entities.details.FileDetails
import com.kio.entities.enums.FileVisibility
import java.time.LocalDate
import java.time.LocalDateTime

data class FileDTO(
    val id: String,
    val ownerId: String,
    val name: String,
    val size: Long,
    val contentType: String,
    val isFavorite: Boolean,
    val details: FileDetails,
    val createdAt: String,
    val lastModified: String,
    val visibility: FileVisibility
)