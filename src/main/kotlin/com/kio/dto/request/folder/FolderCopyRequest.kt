package com.kio.dto.request

import com.kio.shared.enums.FileCopyStrategy
import com.kio.shared.enums.FolderCopyStrategy
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class FolderCopyRequest(

    @get:NotBlank
    val source: String,

    @get:NotBlank
    val destination: String,

    @get:NotNull
    val folderCopyStrategy: FolderCopyStrategy,

    @get:NotNull
    val fileCopyStrategy: FileCopyStrategy
)