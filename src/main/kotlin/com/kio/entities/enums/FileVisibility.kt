package com.kio.entities.enums

enum class FileVisibility {
    OWNER, // only the owner of the resource can see it
    RESTRICTED, // only the owner and the resource's contributors can see it
    PUBLIC // everyone can see it, even non authenticated users
}