package com.kio.services

import com.kio.dto.request.contributor.ContributorDeleteRequest
import com.kio.dto.request.contributor.ContributorUpdatePermissionsRequest
import com.kio.dto.request.contributor.ContributorAddRequest
import com.kio.dto.request.contributor.ContributorExistsRequest
import com.kio.dto.response.ContributorResponseDTO
import com.kio.dto.response.UserDTO
import com.kio.entities.Folder
import com.kio.mappers.UserMapper
import com.kio.repositories.FolderRepository
import com.kio.repositories.UserRepository
import com.kio.shared.exception.IllegalOperationException
import com.kio.shared.exception.NotFoundException
import com.kio.shared.utils.PermissionValidatorUtil
import com.kio.shared.utils.SecurityUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class ContributorService(
    private val folderRepository: FolderRepository,
    private val userRepository: UserRepository
) {

    @Value(value = "\${kio.contributors.page-size}")
    private var pageSize: Int? = null

    @Value(value = "\${kio.contributors.preview-page-size}")
    private var previewPageSize: Int? = null

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

    fun doesContributorExists(request: ContributorExistsRequest) {
        val folder = this.findFolderById(request.folderId)
        val containsContributor = folder.contributors.keys.contains(request.contributorId)
        if (!containsContributor) {
            throw NotFoundException("Folder ${request.folderId} does not contain contributor ${request.contributorId}")
        }
    }

    fun findFolderContributors(folderId: String, page: Int): Page<UserDTO> {
        val folder = this.findFolderById(folderId)
        val contributors = userRepository.findByIdIn(
            ids = folder.contributors.keys,
            pageRequest = PageRequest.of(page, pageSize!!, Sort.by(Sort.Direction.ASC, "email") )
        )
        return contributors.map { UserMapper.toUserDTO(it) }
    }

    fun findFolderContributorsPreview(folderId: String): ContributorResponseDTO {
        val folder = this.findFolderById(folderId)
        val usersPage = userRepository.findByIdIn(
            ids = folder.contributors.keys,
            pageRequest = PageRequest.of(0, previewPageSize!!, Sort.by(Sort.Direction.ASC, "email"))
        ).map { UserMapper.toUserDTO(it) }

        return ContributorResponseDTO(
            content = usersPage.content,
            totalContributors = folder.contributors.keys.size
        )
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