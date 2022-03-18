package com.kio.dto.request

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty

data class GenericResourceRequest (
    @NotBlank val parentFolder: String,
    @NotEmpty val resources: List<String>,
)