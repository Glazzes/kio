package com.kio.dto.response.find

data class FileDTO(
    val id: String,
    val name: String,
    val size: Long,
    val contentType: String
)