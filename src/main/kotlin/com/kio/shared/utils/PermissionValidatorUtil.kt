package com.kio.shared.utils

import com.kio.entities.File
import com.kio.entities.Folder
import com.kio.entities.enums.FileVisibility
import com.kio.entities.enums.Permission
import com.kio.shared.exception.IllegalOperationException
import com.kio.shared.exception.UnIdentifiedResourceException
import org.springframework.stereotype.Component

object PermissionValidatorUtil {

    fun isResourceOwner(resource: Any) {
        val authenticatedUser = SecurityUtil.getAuthenticatedUser()

        val isOwner = when (resource) {
            is File -> resource.metadata.ownerId == authenticatedUser.id!!
            is Folder -> resource.metadata.ownerId == authenticatedUser.id!!
            else -> throw UnIdentifiedResourceException("We could not identify the ownership of this resource")
        }

        if(!isOwner) {
            throw IllegalOperationException("You can perform any action on this resource as it does belong to you.")
        }
    }

    fun isFoldersOwner(folder: Folder): Boolean {
        val authenticatedUser = SecurityUtil.getAuthenticatedUser()
        return authenticatedUser.id!! == folder.metadata.ownerId
    }

    fun checkFolderPermissions(folder: Folder, vararg requiredPermissions: Permission) {
        val authenticatedUser = SecurityUtil.getAuthenticatedUser()
        if(authenticatedUser.id == folder.metadata.ownerId) return
        if(folder.visibility == FileVisibility.PUBLIC) return

        if(folder.visibility == FileVisibility.OWNER) {
            throw IllegalAccessException("Only the owner of this resource can see its contents.")
        }

        val contributorPermissions = folder.contributors[authenticatedUser.id!!] ?:
            throw IllegalAccessException("This resource has not been shared with you.")

        if(contributorPermissions.containsAll(requiredPermissions.asList())) {
            throw IllegalOperationException("You do not have the required permissions to alter this resource ${folder.id}")
        }
    }

}