package com.kio.entities

import com.kio.shared.enums.ResourceType

class SharedResource (
    val resourceId: String,
    val type: ResourceType,
) : java.io.Serializable