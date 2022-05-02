package com.kio.services

import com.kio.dto.ContributorInfo
import com.kio.dto.request.NewContributorRequest
import com.kio.entities.Folder
import com.kio.entities.User
import com.kio.entities.enums.FileState
import com.kio.mappers.UserMapper
import com.kio.repositories.FolderRepository
import com.kio.repositories.UserRepository
import com.kio.shared.exception.AlreadyExistsException
import com.kio.shared.exception.IllegalOperationException
import com.kio.shared.exception.NotFoundException
import org.springframework.stereotype.Service

@Service
class ContributorService(
    private val folderRepository: FolderRepository,
    private val userRepository: UserRepository
) {

    fun save(folderId: String, contributorRequest: NewContributorRequest): ContributorInfo {
        val folder = this.findFolderById(folderId)
        this.checkResourceOwnership(folder)

        val contributor = this.findContributorById(contributorRequest.contributorId)
        if(folder.contributors.containsKey(contributor.id!!)) {
            throw AlreadyExistsException("This user is already a contributor of this folder")
        }

        folder.contributors[contributor.id!!] = contributorRequest.permissions
        if(folder.state != FileState.PUBLIC) folder.state = FileState.RESTRICTED
        folderRepository.save(folder)

        return UserMapper.toContributorInfo(contributor)
    }

    private fun checkResourceOwnership(folder: Folder) {
        val isOwner = PermissionValidator.isResourceOwner(folder)
        if(!isOwner) {
            throw IllegalOperationException("You can not add contributors to a resource you do not own")
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