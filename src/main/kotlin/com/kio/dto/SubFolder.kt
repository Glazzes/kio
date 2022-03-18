package com.kio.dto

data class SubFolder(
    val id: String,
    val name: String,
    val color: String,
    val contributors: Set<Contributor>
)