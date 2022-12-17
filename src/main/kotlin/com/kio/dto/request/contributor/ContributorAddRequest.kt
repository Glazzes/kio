package com.kio.dto.request.contributor

import com.kio.entities.enums.Permission
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty

data class ContributorAddRequest(
    @get:NotBlank(message = "{constraints.contributor.add.source.required}")
    val folderId: String,

    @get:NotEmpty(message = "{constraints.contributor.add.users.required}")
    val contributorIds: Collection<String>,

    @get:NotEmpty(message = "{constraints.contributor.add.permissions.required}")
    val permissions: Set<Permission>
)
