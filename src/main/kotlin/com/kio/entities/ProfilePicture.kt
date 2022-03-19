package com.kio.entities

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate

@Document(collection = "profile_pictures")
class ProfilePicture(
    @Id var id: String? = null,
    val owner: String,
    val isActive: Boolean = true,
    val url: String,
    @CreatedDate var createdAt: LocalDate? = null,
    @LastModifiedDate var lastTimeUsed: LocalDate? = null
)