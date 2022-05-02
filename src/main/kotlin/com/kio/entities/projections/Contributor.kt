package com.kio.entities.projections

import com.kio.entities.ProfilePicture

data class ContributorProjection(
    val id: String,
    val username: String,
    val profilePicture: ProfilePicture,
)