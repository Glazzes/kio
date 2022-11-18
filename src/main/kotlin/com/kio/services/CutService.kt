package com.kio.services

import com.kio.dto.request.file.FileCopyRequest
import com.kio.dto.response.ContributorDTO
import com.kio.dto.response.FileDTO
import com.kio.dto.response.FolderDTO
import com.kio.entities.details.FileMetadata
import com.kio.entities.File
import com.kio.entities.Folder
import com.kio.entities.enums.Permission
import com.kio.mappers.FileMapper
import com.kio.mappers.FolderMapper
import com.kio.repositories.FileRepository
import com.kio.repositories.FolderRepository
import com.kio.repositories.UserRepository
import com.kio.shared.exception.IllegalOperationException
import com.kio.shared.exception.NotFoundException
import com.kio.shared.utils.PermissionValidatorUtil
import org.springframework.stereotype.Service

@Service
class CutService(
    private val folderRepository: FolderRepository,
    private val fileRepository: FileRepository,
    private val userRepository: UserRepository,
    private val copyUtilService: CopyUtilService,
){

    fun cutFolders(cutRequest: FileCopyRequest): Collection<FolderDTO> {
        val source = this.findFolderById(cutRequest.from)
        val destination = this.findFolderById(cutRequest.to)

        this.verifyCutFolderOperationPermissions(source, destination)

        val foldersToCut = folderRepository.findByIdIsIn(cutRequest.items)
        return this.performCutFolderOperation(source, destination, foldersToCut)
    }

    private fun verifyCutFolderOperationPermissions(source: Folder, destination: Folder) {
        if(source.id == destination.id) {
            throw IllegalOperationException("You can not cut a folder and save them into the same folder they come from")
        }

        PermissionValidatorUtil.isResourceOwner(source)
        PermissionValidatorUtil.verifyFolderPermissions(destination, Permission.READ_WRITE)
        copyUtilService.canCutFolder(source, destination)
    }

    private fun performCutFolderOperation(source: Folder, destination: Folder, folders: Collection<Folder>): Collection<FolderDTO> {
        val cutSize = folders.sumOf { it.summary.size }
        val folderIds = folders.map { it.id!! }.toSet()

        source.apply {
            summary.size = summary.size - cutSize
            summary.folders = summary.folders - folders.size
            subFolders.removeAll(folderIds)
        }

        destination.apply {
            summary.size += cutSize
            summary.folders += folders.size
            subFolders.addAll(folderIds)
        }

        val cutFolders = folders.map { it.apply {
            parentFolder = destination.id
        } }

        folderRepository.saveAll(mutableSetOf(source, destination))
        return folderRepository.saveAll(cutFolders)
            .map { FolderMapper.toFolderDTO(it, emptyList()) }
    }

    fun cutFiles(copyRequest: FileCopyRequest): Collection<FileDTO> {
        val source = this.findFolderById(copyRequest.from)
        val destination = this.findFolderById(copyRequest.to)

        this.verifyCutFilesOperationPermissions(source, destination, copyRequest.items)

        val filesToCopy = fileRepository.findByIdIsIn(copyRequest.items)
        return this.performCutFilesOperation(source, destination, filesToCopy)
    }

    private fun verifyCutFilesOperationPermissions(source: Folder, destination: Folder, files: Collection<String>) {
        if(source.id == destination.id) {
            throw IllegalOperationException("You can not cut files and save them into the same folder they come from")
        }

        if(!source.files.containsAll(files)) {
            throw IllegalOperationException("At least one of the files to cut does not belong to source")
        }

        PermissionValidatorUtil.isResourceOwner(source)
        PermissionValidatorUtil.verifyFolderPermissions(destination, Permission.READ_WRITE)
    }

    private fun performCutFilesOperation(
        source: Folder,
        destination: Folder,
        filesToCut: Collection<File>,
    ): Collection<FileDTO> {
        val cutSize = filesToCut.sumOf { it.size }
        val fileIds = filesToCut.map { it.id!! }

        source.apply {
            summary.files = summary.files - filesToCut.size
            summary.size -= cutSize
            files.removeAll(fileIds.toSet())
        }

        destination.apply {
            summary.files += filesToCut.size
            summary.size += cutSize
            files.addAll(fileIds.toSet())
        }

        val modifiedFiles = filesToCut.map { it.apply {
            parentFolder = destination.id!!
            metadata = FileMetadata(destination.metadata.ownerId)
        } }

        folderRepository.saveAll(mutableListOf(source, destination))

        return fileRepository.saveAll(modifiedFiles)
            .map { FileMapper.toFileDTO(it) }
    }

    // repeated
    private fun findFolderContributors(folder: Folder): Set<ContributorDTO> {
        val contributors = userRepository.findByIdIn(folder.contributors.keys)
        return contributors.map { ContributorDTO(it.id, it.username, "") }
            .toSet()
    }

    private fun findFolderById(id: String): Folder {
        return folderRepository.findById(id)
            .orElseThrow { NotFoundException("Can not find folder with id $id") }
    }

}