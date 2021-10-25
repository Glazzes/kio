package com.kio.dto.find

import java.time.LocalDate

data class FileDTO(
    val id: String?,
    val filename: String,
    val size: Long,
    val lastModified: LocalDate?,
    val createdBy: UserDTO
)