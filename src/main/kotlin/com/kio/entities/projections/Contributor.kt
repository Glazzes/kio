package com.kio.entities.projections

data class ProfilePictureProjection(val url: String)

data class ContributorProjection(val id: String, val username: String, val profilePicture: ProfilePictureProjection)