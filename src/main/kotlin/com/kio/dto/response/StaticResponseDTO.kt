package com.kio.dto.response

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody

data class StaticResponseDTO(
    val contentType: String,
    val responseBody: StreamingResponseBody
)