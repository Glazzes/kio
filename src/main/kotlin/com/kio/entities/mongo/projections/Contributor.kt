package com.kio.entities.mongo.projections

data class ProfilePictureProjection(val url: String)

data class ContributorProjection(val id: String, val username: String, val profilePicture: ProfilePictureProjection)