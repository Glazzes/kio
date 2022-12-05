package com.kio.dto.response

data class ExistsResponseDTO(
    val existsByUsername: Boolean,
    val existsByEmail: Boolean
)
