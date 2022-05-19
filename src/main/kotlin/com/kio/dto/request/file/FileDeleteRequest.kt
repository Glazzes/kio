package com.kio.dto.request.file

data class FileDeleteRequest(
    val from: String,
    val files: Collection<String>
)