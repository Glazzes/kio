package com.kio.entities.details

import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDate
import java.time.LocalDateTime

class FileMetadata(
    val ownerId: String,
    @CreatedBy var createdBy: String? = null,
    @LastModifiedBy var lastModifiedBy: String? = null,
    @CreatedDate var createdAt: LocalDate? = null,
    @LastModifiedDate var lastModifiedDate: LocalDateTime? = null
)