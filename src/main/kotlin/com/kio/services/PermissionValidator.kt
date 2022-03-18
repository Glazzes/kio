package com.kio.services

import com.kio.entities.mongo.Folder
import com.kio.entities.mongo.enums.FileState
import com.kio.entities.mongo.enums.FolderType
import com.kio.entities.mongo.enums.Permission
import com.kio.shared.utils.SecurityUtil

object PermissionValidator {

    fun canPerformOperation(folder: Folder, requiredPermission: Permission): Boolean {
        val currentUser = SecurityUtil.getAuthenticatedUser()
        if(currentUser.id == folder.metadata.owner) return true

        if(folder.folderType == FolderType.ROOT_UNIT) return false
        if(folder.state == FileState.OWNER) return false
        val currentUserPermissions = folder.coowners[currentUser.id!!] ?: return false

        return currentUserPermissions.contains(requiredPermission)
    }

}