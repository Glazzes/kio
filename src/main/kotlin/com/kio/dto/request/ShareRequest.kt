package com.kio.dto.request

import java.util.concurrent.TimeUnit

data class ShareRequest(
    val resource: String,
    val duration: Long,
    val timeUnit: TimeUnit
)