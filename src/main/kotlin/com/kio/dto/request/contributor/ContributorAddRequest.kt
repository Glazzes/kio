package com.kio.dto.request.contributor

import com.kio.entities.enums.Permission
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty

data class ContributorAddRequest(

    @get:NotBlank(message = "Contributor id is required")
    val contributorId: String,

    @get:NotEmpty(message = "All contributors must have at least one permission")
    val permissions: Set<Permission>

)
