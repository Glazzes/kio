package com.kio.valueobjects

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class AudioSamples(
    val version: Int,
    val channels: Int,
    val sampleRate: Int,
    val samplesPerPixel: Int,
    val bits: Int,
    val length: Int,
    val data: Array<Int>
)