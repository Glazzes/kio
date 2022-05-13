package com.kio.dto.response

import java.io.InputStream

data class StaticResponseDTO(
    val contentType: String,
    val inputStream: InputStream
)