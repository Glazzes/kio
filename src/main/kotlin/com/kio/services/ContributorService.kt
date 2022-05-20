package com.kio.services

import com.kio.dto.response.ContributorDTO
import com.kio.dto.request.contributor.ContributorDeleteRequest
import com.kio.dto.request.contributor.ContributorUpdatePermissionsRequest
import com.kio.dto.request.contributor.ContributorAddRequest
import com.kio.entities.Folder
import com.kio.entities.User
import com.kio.entities.enums.FolderType
import com.kio.mappers.UserMapper
import com.kio.repositories.FolderRepository
import com.kio.repositories.UserRepository
import com.kio.shared.exception.AlreadyExistsException
import com.kio.shared.exception.IllegalOperationException
import com.kio.shared.exception.NotFoundException
import com.kio.shared.utils.PermissionValidatorUtil
import org.springframework.stereotype.Service

@Service
class ContributorService(
    private val folderRepository: FolderRepository,
    private val userRepository: UserRepository
) {

    fun save(folderId: String, contributorRequest: ContributorAddRequest): ContributorDTO {
        val folder = this.findFolderById(folderId)
        PermissionValidatorUtil.isResourceOwner(folder)

        if(folder.folderType == FolderType.ROOT) {
            throw IllegalOperationException("You can not share your unit with someone else, consider sharing and inner folder")
        }

        val contributor = this.findContributorById(contributorRequest.contributorId)

        if(folder.contributors.containsKey(contributor.id!!)) {
            throw AlreadyExistsException("This user is already a contributor of this folder, consider updating it")
        }

        folder.apply {
            sharedWith.add(contributorRequest.contributorId)
            contributors[contributor.id!!] = contributorRequest.permissions
        }

        folderRepository.save(folder)

        this.addContributorToSubFolders(folder.subFolders, contributorRequest)
        return UserMapper.toContributorDTO(contributor)
    }

    private fun addContributorToSubFolders(folderIds: Collection<String>, request: ContributorAddRequest) {
        val subFolders = folderRepository.findByIdIsIn(folderIds).map {
            it.apply { it.contributors[request.contributorId] = request.permissions }
        }

        val subFolderIds = subFolders.map { it.subFolders }.flatten()
        folderRepository.saveAll(subFolders)

        if(subFolderIds.isNotEmpty()) {
            addContributorToSubFolders(subFolderIds, request)
        }
    }

    fun update(request: ContributorUpdatePermissionsRequest) {
        val folder = this.findFolderById(request.folderId)
        PermissionValidatorUtil.isResourceOwner(folder)

        folderRepository.save(folder.apply {
            contributors[request.contributorId] = request.permissions.toMutableSet()
        })
    }

    fun delete(request: ContributorDeleteRequest) {
        val (folderId, contributors) = request
        val folder = this.findFolderById(folderId)

        if(!folder.contributors.keys.containsAll(contributors)) {
            throw IllegalOperationException("One or more contributors are no part of this folder $folderId")
        }

        folderRepository.save(folder.apply {
            sharedWith.removeAll(contributors.toSet())
            contributors.forEach { this.contributors.remove(it) }
        })

        this.deleteContributorFromSubFolders(folder.subFolders, request)
    }

    private fun deleteContributorFromSubFolders(folderIds: Collection<String>, request: ContributorDeleteRequest) {
        val subFolders = folderRepository.findByIdIsIn(folderIds).map {
            it.apply {
                this.sharedWith.removeAll(request.contributors.toSet())
                request.contributors.forEach { key -> this.contributors.remove(key) }
            }
        }

        val subFolderIds = subFolders.map { it.subFolders }.flatten()
        folderRepository.saveAll(subFolders)

        if(subFolderIds.isNotEmpty()) {
            deleteContributorFromSubFolders(subFolderIds, request)
        }
    }

    private fun findContributorById(id: String): User {
        return userRepository.findById(id)
            .orElseThrow { NotFoundException("We could not find contributor with $id") }
    }

    private fun findFolderById(id: String): Folder {
        return folderRepository.findById(id)
            .orElseThrow { NotFoundException("We could not found folder with $id") }
    }

}