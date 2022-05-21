package com.kio.entities

import com.kio.entities.details.FileMetadata
import com.kio.entities.details.FolderDetails
import com.kio.entities.enums.FileVisibility
import com.kio.entities.enums.FolderType
import com.kio.entities.enums.Permission
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "folders")
class Folder(
    @Id var id: String? = null,
    var name: String,
    var folderType: FolderType = FolderType.REGULAR,
    var visibility: FileVisibility = FileVisibility.OWNER,
    var color: String = "#3366ff",
    var parentFolder: String? = null,
    val subFolders: MutableSet<String> = HashSet(),
    val files: MutableSet<String> = HashSet(),
    val contributors: MutableMap<String, Set<Permission>> = HashMap(),
    val sharedWith: MutableSet<String> = HashSet(),
    val metadata: FileMetadata,
    val summary: FolderDetails = FolderDetails()
)