package com.kio.entities

import kotlin.math.pow

data class UnitSummary(
    var spaceAvailable: Long = 1024.0.pow(3).toLong(),
    var spaceUsed: Long = 0,
    var numberOfFiles: Long = 0,
    var numberOfFolders: Long = 0,
)