package com.kio.dto.request

import com.kio.entities.enums.Permission

data class NewContributorRequest(
    val contributorId: String,
    val permissions: Set<Permission>
)
