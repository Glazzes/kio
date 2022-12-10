package com.kio.dto.request.contributor

import com.kio.entities.enums.Permission
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty

data class ContributorUpdatePermissionsRequest(
    @get:NotBlank(message = "FolderId is required")
    val folderId: String,

    @get:NotBlank(message = "Contributor id is required")
    val contributorId: String,

    @get:NotEmpty(message = "At least one permission is necessary")
    val permissions: Collection<Permission>
)
