package com.kio.services

import com.kio.dto.request.contributor.ContributorDeleteRequest
import com.kio.dto.request.contributor.ContributorUpdatePermissionsRequest
import com.kio.dto.request.contributor.ContributorAddRequest
import com.kio.dto.response.UserDTO
import com.kio.entities.Folder
import com.kio.entities.User
import com.kio.mappers.UserMapper
import com.kio.repositories.FolderRepository
import com.kio.repositories.UserRepository
import com.kio.shared.exception.IllegalOperationException
import com.kio.shared.exception.NotFoundException
import com.kio.shared.utils.PermissionValidatorUtil
import com.kio.shared.utils.SecurityUtil
import org.springframework.stereotype.Service

@Service
class ContributorService(
    private val folderRepository: FolderRepository,
    private val userRepository: UserRepository
) {

    fun save(request: ContributorAddRequest): Collection<UserDTO> {
        val folder = this.findFolderById(request.folderId)
        val isOwner = PermissionValidatorUtil.isResourceOwner(folder)
        if (!isOwner) {
            throw IllegalOperationException("You can not add a contributor to a folder you do not own")
        }

        val contributors = userRepository.findByIdIn(request.contributorIds)
        val contributorPermissions = contributors.associateBy({it.id!!}, {request.permissions})
        val nestedFolders = this.findAllNestedFolders(folder)
        nestedFolders.add(folder)

        folderRepository.saveAll(nestedFolders.map { it.apply {
            this.contributors.putAll(contributorPermissions)
        }})

        return contributors.map { UserMapper.toUserDTO(it) }
    }

    fun findFolderContributors(folderId: String): Collection<UserDTO> {
        val folder = this.findFolderById(folderId)
        val contributors = userRepository.findByIdIn(folder.contributors.keys)
        return contributors.map { UserMapper.toUserDTO(it) }
    }

    fun update(request: ContributorUpdatePermissionsRequest) {
        val folder = this.findFolderById(request.folderId)
        PermissionValidatorUtil.isResourceOwner(folder)

        folderRepository.save(folder.apply {
            contributors[request.contributorId] = request.permissions.toMutableSet()
        })
    }

    fun deleteContribtorByThemselves(folderId: String) {
        val authenticatedUser = SecurityUtil.getAuthenticatedUser()
        val folder = this.findFolderById(folderId)
        val isContributor = PermissionValidatorUtil.isContributor(folder)

        if(!isContributor) {
            throw NotFoundException("You're not a contributor of this folder ${folder.id}")
        }

        folderRepository.save(folder.apply {
            contributors.remove(authenticatedUser.id!!)
        })
    }

    fun delete(request: ContributorDeleteRequest) {
        val folder = this.findFolderById(request.folderId)
        val isOwner = PermissionValidatorUtil.isResourceOwner(folder)
        if(!isOwner) {
            throw IllegalOperationException("Can not delete a contributor from a folder you do not own")
        }

        if(!folder.contributors.keys.containsAll(request.contributors)) {
            throw IllegalOperationException("One or more contributors are no part of this folder ${request.folderId}")
        }

        val nestedFolders = this.findAllNestedFolders(folder)
        nestedFolders.add(folder)

        val foldersToDelete = nestedFolders.map { it.apply {
            this.contributors.forEach { entry -> this.contributors.remove(entry.key) }
        } }

        folderRepository.saveAll(foldersToDelete)
    }

    private fun findAllNestedFolders(parentFolder: Folder): MutableCollection<Folder> {
        if(parentFolder.subFolders.isEmpty()) {
            return mutableSetOf()
        }

        val context: MutableSet<Folder> = HashSet()
        val subFolders = folderRepository.findByIdIsIn(parentFolder.subFolders)
        for(subfolder in subFolders) {
            val result = this.findAllNestedFolders(subfolder)
            context.add(subfolder)
            context.addAll(result)
        }

        return context
    }

    private fun findFolderById(id: String): Folder {
        return folderRepository.findById(id)
            .orElseThrow { NotFoundException("We could not found folder with $id") }
    }

}