package com.kio.entities

import com.kio.entities.enums.FileState
import com.kio.entities.enums.FolderType
import com.kio.entities.enums.Permission
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "folders")
class Folder(
    @Id var id: String? = null,
    var name: String,
    val size: Long = 0,
    var folderType: FolderType = FolderType.REGULAR,
    var state: FileState = FileState.OWNER,
    var color: String = "grey200",
    val files: MutableSet<String> = HashSet(),
    val subFolders: MutableSet<String> = HashSet(),
    val coowners: MutableMap<String, Set<Permission>> = HashMap(),
    val sharedWith: MutableSet<String> = HashSet(),
    val metadata: AuditFileMetadata,
)