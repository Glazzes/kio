package com.kio.entities

import com.kio.entities.details.FileDetails
import com.kio.entities.details.FileMetadata
import com.kio.entities.enums.FileVisibility
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "files")
class File(
    @Id
    var id: String? = null,
    var name: String,
    val details: FileDetails,
    val contentType: String,
    val size: Long,
    val bucketKey: String,
    var parentFolder: String,
    val favorites: MutableSet<String> = HashSet(),
    var visibility: FileVisibility,
    var metadata: FileMetadata,
)