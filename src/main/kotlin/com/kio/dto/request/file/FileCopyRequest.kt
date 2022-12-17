package com.kio.dto.request.file

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty

data class FileCopyRequest(
    @get:NotBlank(message = "{constraints.copy.source.required}")
    val from: String,

    @get:NotBlank(message = "{constraints.copy.destination.required}")
    val to: String,

    @get:NotEmpty(message = "{constraints.copy.items.required}")
    val items: Collection<String>,
)
