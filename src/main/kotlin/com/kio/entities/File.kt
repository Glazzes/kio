package com.kio.entities

import com.kio.entities.enums.FileState
import org.springframework.data.mongodb.core.mapping.Document
import javax.persistence.Id

@Document(collection = "files")
class File(
    @Id
    var id: String? = null,
    var name: String,
    val contentType: String,
    val size: Long,
    val bucketKey: String,
    val url: String,
    var parentFolder: String,
    var state: FileState = FileState.OWNER,
    var metadata: AuditFileMetadata,
)