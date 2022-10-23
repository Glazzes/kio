package com.kio.entities.details

data class FileDetails(
    val dimensions: String? = null,
    val duration: String? = null,
    val audioSamples: Array<Int>? = null,
    // val waveFormBucketKey: String? = null,
    val pages: Int? = null
)