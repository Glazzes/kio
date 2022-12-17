package com.kio.dto.request.contributor

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty

data class ContributorDeleteRequest(
    @get:NotBlank(message = "{constraints.contributor.delete.source.required}")
    val folderId: String,

    @get:NotEmpty(message = "{constraints.contributor.delete.users.required}")
    val contributors: Collection<String>
)
