package com.kio.services

import com.kio.entities.File
import com.kio.entities.Folder
import com.kio.entities.enums.FileState
import com.kio.entities.enums.FolderType
import com.kio.entities.enums.Permission
import com.kio.shared.exception.UnIdentifiedResourceException
import com.kio.shared.utils.SecurityUtil

object PermissionValidator {

    fun isResourceOwner(resource: Any): Boolean {
        val user = SecurityUtil.getAuthenticatedUser()

        return when (resource) {
            is File -> resource.metadata.owner == user.id!!
            is Folder -> resource.metadata.owner == user.id!!
            else -> throw UnIdentifiedResourceException("We could not identify the ownership of the resource")
        }
    }

    fun canPerformOperation(folder: Folder, requiredPermission: Permission): Boolean {
        val currentUser = SecurityUtil.getAuthenticatedUser()
        if(currentUser.id == folder.metadata.owner) return true

        if(folder.folderType == FolderType.ROOT_UNIT) return false
        if(folder.state == FileState.OWNER) return false
        val currentUserPermissions = folder.coowners[currentUser.id!!] ?: return false

        return currentUserPermissions.contains(requiredPermission)
    }

}