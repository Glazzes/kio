package com.kio.dto.request

import com.kio.shared.enums.FileCopyStrategy
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class FileCopyRequest(

    @get:NotBlank(message = "Source id must not be a blank string")
    val sourceId: String,

    @get:NotBlank(message = "Destination id must not be a blank string")
    val destinationId: String,

    @get:NotEmpty(message = "At least one file it's required to perform the copy/cut operation")
    val files: Collection<String>,

    @get:NotNull(message = "A copy strategy is required")
    val strategy: FileCopyStrategy

)
