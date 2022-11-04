package com.kio.dto.request.file

import com.kio.valueobjects.UploadMetadataRequest

data class FileUploadRequest(
    val to: String,
    val details: Map<String, UploadMetadataRequest>
)