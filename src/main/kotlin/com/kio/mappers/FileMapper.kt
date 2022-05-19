package com.kio.mappers

import com.kio.dto.response.FileDTO
import com.kio.entities.File

object FileMapper {

    fun toFileDTO(file: File) = FileDTO(
        id = file.id!!,
        name = file.name,
        size = file.size,
        contentType = file.contentType,
        details = file.details,
        createdAt = file.metadata.createdAt!!,
        lastModified = file.metadata.lastModifiedDate!!
    )

}