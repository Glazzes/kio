package com.kio.entities.details

data class FileDetails(
    val dimensions: Array<Int>? = null,
    val duration: Int? = null,
    var audioSamples: Array<Int>? = null,
    var pages: Int? = null,
    var thumbnailKey: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileDetails

        if (dimensions != null) {
            if (other.dimensions == null) return false
            if (!dimensions.contentEquals(other.dimensions)) return false
        } else if (other.dimensions != null) return false
        if (audioSamples != null) {
            if (other.audioSamples == null) return false
            if (!audioSamples.contentEquals(other.audioSamples)) return false
        } else if (other.audioSamples != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = dimensions?.contentHashCode() ?: 0
        result = 31 * result + (audioSamples?.contentHashCode() ?: 0)
        return result
    }
}