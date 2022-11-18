package com.kio.dto.request.file

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty

data class FileCopyRequest(

    @get:NotBlank(message = "Source id must not be a blank string")
    val from: String,

    @get:NotBlank(message = "Destination id must not be a blank string")
    val to: String,

    @get:NotEmpty(message = "At least one file it's required to perform the copy/cut operation")
    val items: Collection<String>,
)
