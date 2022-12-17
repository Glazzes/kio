package com.kio.valueobjects

import com.kio.entities.*

data class DeleteContent (
    val fileIds: MutableCollection<String> = HashSet(),
    val folders: MutableCollection<Folder> = HashSet()
)