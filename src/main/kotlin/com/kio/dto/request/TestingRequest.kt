package com.kio.dto.request

import org.springframework.web.multipart.MultipartFile

data class TestingRequest (
    val body: String,
    val files: Collection<MultipartFile>
)