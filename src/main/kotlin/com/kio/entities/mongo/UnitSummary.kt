package com.kio.entities.mongo

data class UnitSummary(
    var spaceUsed: Long = 0,
    var numberOfFiles: Long = 0,
    var numberOfFolders: Long = 0,
)