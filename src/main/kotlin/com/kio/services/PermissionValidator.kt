package com.kio.services

import com.kio.entities.File
import com.kio.entities.Folder
import com.kio.entities.enums.FileState
import com.kio.entities.enums.FolderType
import com.kio.entities.enums.Permission
import com.kio.shared.exception.IllegalOperationException
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

    fun checkResourcePermissions(folder: Folder, requiredPermission: Permission) {
        val errorMessage = when(requiredPermission) {
            Permission.CAN_READ -> "You're not allowed to view this resource"
            Permission.CAN_CREATE -> "You're not allowed to create folder or files within this folder"
            Permission.CAN_MODIFY -> "You're not allowed to modify this resource"
            Permission.CAN_DELETE_FILE -> "You're not allowed to delete resource(s)"
            else -> "You're not allowed to perform any action on this resource"
        }

        val canReadFolder = isAllowedToPerformAction(folder, requiredPermission)
        if(!canReadFolder) {
            throw IllegalOperationException(errorMessage)
        }
    }

     private fun isAllowedToPerformAction(folder: Folder, requiredPermission: Permission): Boolean {
        val currentUser = SecurityUtil.getAuthenticatedUser()
        if(currentUser.id == folder.metadata.owner) return true

        if(folder.folderType == FolderType.ROOT_UNIT) return false
        if(folder.state == FileState.OWNER) return false
        val currentUserPermissions = folder.contributors[currentUser.id!!] ?: return false

        return currentUserPermissions.contains(requiredPermission)
    }

}