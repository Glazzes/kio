package com.kio.dto.response

data class ContributorResponseDTO(
    val content: Collection<UserDTO>,
    val totalContributors: Int
)
