package com.kio.dto.request

import javax.validation.constraints.NotBlank

data class FavoriteRequest(
    @get:NotBlank(message = "Resource id is mandatory")
    val resourceId: String,
    val favorite: Boolean,
)