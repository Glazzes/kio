package com.kio.entities.enums

enum class FolderType {
    ROOT, // represents the user's unit root folder which can not be created, renamed, deleted or shared
    REGULAR // as it implies it's a folder that can be modified and deleted
}