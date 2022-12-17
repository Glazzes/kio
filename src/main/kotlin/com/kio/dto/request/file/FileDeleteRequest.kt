package com.kio.dto.request.file

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty

data class FileDeleteRequest(
    @get:NotBlank(message = "{constraints.file.delete.source.required}")
    val from: String,

    @get:NotEmpty(message = "{constraints.file.delete.items.required}")
    val files: Collection<String>
)