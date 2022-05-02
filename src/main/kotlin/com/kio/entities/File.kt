package com.kio.entities

import com.kio.entities.enums.FileState
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "files")
class File(
    @Id
    var id: String? = null,
    var name: String,
    val contentType: String,
    val size: Long,
    val bucketKey: String,
    var parentFolder: String,
    var state: FileState,
    var metadata: AuditFileMetadata,
)