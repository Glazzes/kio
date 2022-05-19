package com.kio.dto.request

import com.kio.entities.enums.Permission
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty

data class NewContributorRequest(

    @get:NotBlank(message = "Contributor id must not be a blank string")
    val contributorId: String,

    @get:NotEmpty(message = "All contributors must have at least one permission")
    val permissions: Set<Permission>

)
