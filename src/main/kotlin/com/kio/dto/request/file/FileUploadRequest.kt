package com.kio.dto.request.file

import com.kio.entities.details.FileDetails

data class FileUploadRequest(
    val to: String,
    val details: Map<String, FileDetails>
)