package com.kio.entities

import com.kio.entities.enums.Plan
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "users")
class User (
    @Id var id: String? = null,
    var username: String,
    var password: String,
    var email: String,
    var profilePicture: ProfilePicture,
    var plan: Plan = Plan.BASIC
)