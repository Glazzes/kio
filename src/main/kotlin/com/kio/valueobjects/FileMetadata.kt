package com.kio.valueobjects

data class FileMetadata(
    val duration: Int?,
    val width: Int?,
    val height: Int?,
    val pages: Int?,
    val thumbnail: String?,
    val samples: Array<Int>?
)