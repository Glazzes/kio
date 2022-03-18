package com.kio.entities.mongo

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "users")
class User (
    @Id var id: String? = null,
    var username: String,
    var password: String,
    var email: String,
    var profilePicture: ProfilePicture?,
    val unitSummary: UnitSummary
)