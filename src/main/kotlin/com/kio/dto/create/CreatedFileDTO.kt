package com.kio.dto.create

import java.time.LocalDate

data class CreatedFileDTO(
    val id: String?,
    val filename: String,
    val size: Long,
    val contentType: String?,
    val createdAt: LocalDate?
)