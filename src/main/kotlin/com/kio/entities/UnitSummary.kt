package com.kio.entities

data class UnitSummary(
    var spaceUsed: Long = 0,
    var numberOfFiles: Long = 0,
    var numberOfFolders: Long = 0,
)