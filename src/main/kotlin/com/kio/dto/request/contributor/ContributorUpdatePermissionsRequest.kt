package com.kio.dto.request

import com.kio.entities.enums.Permission

data class ContributorUpdatePermissionsRequest(
    val folderId: String,
    val contributorId: String,
    val permissions: Collection<Permission>
)
