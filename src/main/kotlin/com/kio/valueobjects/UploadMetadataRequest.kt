package com.kio.valueobjects

data class UploadMetadataRequest(
    val dimensions: Array<Int>? = null,
    val duration: Int? = null,
    val thumbnailName: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UploadMetadataRequest

        if (dimensions != null) {
            if (other.dimensions == null) return false
            if (!dimensions.contentEquals(other.dimensions)) return false
        } else if (other.dimensions != null) return false

        return true
    }

    override fun hashCode(): Int {
        return dimensions?.contentHashCode() ?: 0
    }
}
