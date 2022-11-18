package com.kio.mappers

import com.kio.dto.response.FileDTO
import com.kio.entities.File
import java.time.format.DateTimeFormatter
import java.util.*

object FileMapper {

    private val formatter = DateTimeFormatter.ofPattern("d MMM, yyyy", Locale.ENGLISH)

    fun toFileDTO(file: File) = FileDTO(
        id = file.id!!,
        ownerId = file.metadata.ownerId,
        name = file.name,
        size = file.size,
        contentType = file.contentType,
        details = file.details,
        createdAt = file.metadata.createdAt!!.format(formatter),
        lastModified = file.metadata.lastModifiedDate!!.format(formatter)
    )

}