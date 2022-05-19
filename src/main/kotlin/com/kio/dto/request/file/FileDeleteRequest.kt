package com.kio.dto.request

data class FileDeleteRequest(
    val from: String,
    val files: Collection<String>
)