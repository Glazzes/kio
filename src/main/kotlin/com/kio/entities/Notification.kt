package com.kio.entities

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "notifications")
class Notification(
    @Id var id: String? = null,
)