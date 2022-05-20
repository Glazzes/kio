package com.kio.entities.enums

import kotlin.math.pow

enum class Plan(val space: Long) {
    BASIC(1024.0.pow(3).toLong()),
    PRO(5 * 1024.0.pow(3).toLong()),
    PREMIUM(10 * 1024.0.pow(3).toLong());
}