package com.kio.dto.request.contributor

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty

data class ContributorDeleteRequest(

    @get:NotBlank(message = "Folder id must not be blank")
    val folderId: String,

    @get:NotEmpty(message = "At least one contributor it's necessary to perform the operation")
    val contributors: Collection<String>
)
