package com.kio.entities.details

data class FolderDetails(
    var files: Long = 0,
    var subFolders: Long= 0,
    var size: Long = 0
)