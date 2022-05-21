package com.kio.services

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.DeleteObjectsRequest
import com.kio.dto.request.file.FileCopyRequest
import com.kio.dto.request.folder.FolderCopyRequest
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
import com.kio.shared.enums.FileCopyStrategy
import com.kio.shared.enums.FolderCopyStrategy
import com.kio.shared.exception.AlreadyExistsException
import com.kio.shared.exception.IllegalOperationException
import com.kio.shared.exception.NotFoundException
import com.kio.shared.utils.FileUtils
import com.kio.shared.utils.PermissionValidatorUtil
import org.springframework.stereotype.Service

@Service
class CutService(
    private val folderRepository: FolderRepository,
    private val fileRepository: FileRepository,
    private val userRepository: UserRepository,
    private val copyCheckService: CopyCheckService,
    private val s3: AmazonS3
){

    fun cutFolder(request: FolderCopyRequest): Collection<FolderDTO> {
        val source = this.findFolderById(request.source)
        val destination = this.findFolderById(request.destination)

        PermissionValidatorUtil.isResourceOwner(source)
        PermissionValidatorUtil.checkFolderPermissions(destination, Permission.READ_WRITE)
        copyCheckService.canCutFolder(source, destination)

        if(source.id == destination.id) {
            throw IllegalOperationException("You can not cut a folder and save them into the same folder they come from")
        }

        if(request.folderCopyStrategy == FolderCopyStrategy.OMIT) {
            this.cutFolderWithOmitStrategy(source, destination, request)
            return emptyList()
        }

        this.cutFolderWithMixStrategy(source, destination, request)
        return emptyList()
    }

    private fun cutFolderWithOmitStrategy(source: Folder, destination: Folder, request: FolderCopyRequest): FolderDTO {
        val destinationSubFolders = folderRepository.findByIdIsIn(destination.subFolders)
            .associateBy { it.name }

        if(destinationSubFolders.containsKey(source.name)) {
            throw AlreadyExistsException("No files were copied as a folder with name ${source.name} already exists")
        }

        val sourceParent = this.findFolderById(source.parentFolder!!)
        folderRepository.save(sourceParent.apply { subFolders.remove(request.source) })
        folderRepository.save(destination.apply { subFolders.add(request.source) })
        return FolderMapper.toFolderDTO(destination, this.findFolderContributors(destination))
    }

    private fun cutFolderWithMixStrategy(source: Folder, destination: Folder, request: FolderCopyRequest) {
        val sourceParent = this.findFolderById(source.parentFolder!!)
        val destinationSubFolders = folderRepository.findByIdIsIn(destination.subFolders)
            .associateBy { it.name }

        val destinationSubFolder = destinationSubFolders[source.name]

        if(destinationSubFolder == null) {
            sourceParent.subFolders.remove(source.id!!)
            destination.subFolders.add(source.id!!)
            source.parentFolder = destination.id!!

            folderRepository.saveAll(listOf(sourceParent, source, destination))
            return
        }

        val sourceFiles = fileRepository.findByIdIsIn(source.files)
        val destinationFiles = fileRepository.findByIdIsIn(destinationSubFolder.files)

        if(request.fileCopyStrategy == FileCopyStrategy.OVERWRITE) {
            this.cutFilesWithOverwriteStrategy(source, destinationSubFolder, sourceFiles, destinationFiles)
        }

        if(request.fileCopyStrategy == FileCopyStrategy.RENAME) {
            this.cutFilesWithRenameStrategy(source, destinationSubFolder, sourceFiles, destinationFiles)
        }

        val sourceSubFolders = folderRepository.findByIdIsIn(source.subFolders)
            .associateBy { it.name }

        val destinationPlusOneLevel = folderRepository.findByIdIsIn(destinationSubFolders.map { it.value.id!! })
            .associateBy { it.name }

        for(sourceSubFolder in sourceSubFolders) {
            val destinationSourceEquivalent = destinationPlusOneLevel[sourceSubFolder.key]

            if(destinationSourceEquivalent == null) {
                val dest = destinationSubFolders[source.name]!!.apply {
                    subFolders.add(sourceSubFolder.value.id!!)
                }

                sourceSubFolder.value.apply { parentFolder =  dest.id!! }
                folderRepository.saveAll(listOf(source, sourceSubFolder.value, dest))
                continue
            }

            this.cutFolderWithMixStrategy(sourceSubFolder.value, destinationSourceEquivalent, request)
        }

        folderRepository.delete(source)
    }

    fun cutFiles(request: FileCopyRequest): Collection<FileDTO> {
        val source = this.findFolderById(request.sourceId)
        val destination = this.findFolderById(request.destinationId)

        PermissionValidatorUtil.isResourceOwner(source)
        PermissionValidatorUtil.checkFolderPermissions(destination, Permission.READ_WRITE)

        if(source.id == destination.id) {
            throw IllegalOperationException("You can not cut files and save them into the same folder they come from")
        }

        if(!source.files.containsAll(request.files.toSet())) {
            throw IllegalOperationException("At least one of the files to cut does not belong to source")
        }

        val sourceFiles = fileRepository.findByIdIsIn(request.files)
        val destinationFiles = fileRepository.findByIdIsIn(destination.files)

        if(request.strategy == FileCopyStrategy.OVERWRITE) {
            return this.cutFilesWithOverwriteStrategy(source, destination, sourceFiles, destinationFiles)
        }

        return this.cutFilesWithRenameStrategy(source, destination, sourceFiles, destinationFiles)
    }

    private fun cutFilesWithOverwriteStrategy(
        source: Folder,
        destination: Folder,
        sourceFiles: Collection<File>,
        destinationFiles: Collection<File>
    ): Collection<FileDTO> {
        val sourceFileNames = sourceFiles.map { it.name }.toSet()
        val sourceIdsToDelete = sourceFiles.map { it.id!! }.toSet()
        val destinationFilesToDelete = destinationFiles.filter { sourceFileNames.contains(it.name) }

        val sourceFilesSize = sourceFiles.sumOf { it.size }
        val destinationFilesToDeleteSize = destinationFilesToDelete.sumOf { it.size }

        val cutFiles = sourceFiles.map { it.apply {
            parentFolder = destination.id!!
            metadata = FileMetadata(destination.metadata.ownerId)
        } }

        if(destinationFilesToDelete.isNotEmpty()) {
            val deleteObjects = DeleteObjectsRequest("files.kio.com").apply {
                keys = destinationFilesToDelete.map { DeleteObjectsRequest.KeyVersion(it.bucketKey) }
            }

            s3.deleteObjects(deleteObjects)
        }

        folderRepository.save(source.apply {
            this.files.removeAll(sourceIdsToDelete)
            this.summary.files += (cutFiles.size - destinationFilesToDelete.size)
            this.summary.size -= sourceFilesSize
        })

        folderRepository.save(destination.apply {
            files.addAll(sourceIdsToDelete)
            this.summary.size += (sourceFilesSize - destinationFilesToDeleteSize)
        })

        fileRepository.deleteAll(destinationFilesToDelete)

        return fileRepository.saveAll(cutFiles)
            .map { FileMapper.toFileDTO(it) }
    }

    private fun cutFilesWithRenameStrategy(
        source: Folder,
        destination: Folder,
        sourceFiles: Collection<File>,
        destinationFiles: Collection<File>
    ): Collection<FileDTO> {
        val destinationFileNames = destinationFiles.map { it.name }.toSet()
        val sourceIdsToDelete = sourceFiles.map { it.id!! }.toSet()
        val sourceFilesSize = sourceFiles.sumOf { it.size }

        val cutFiles = sourceFiles.map { it.apply {
            name = FileUtils.getValidName(this.name, destinationFileNames)
            parentFolder = destination.id!!
            metadata = FileMetadata(destination.metadata.ownerId)
        } }

        val newFileIds = cutFiles.map { it.id!! }.toSet()

        folderRepository.save(source.apply { files.removeAll(sourceIdsToDelete) })
        folderRepository.save(destination.apply {
            this.files.addAll(newFileIds)
            this.summary.size += sourceFilesSize
            this.summary.files += cutFiles.size
        })
        return fileRepository.saveAll(cutFiles)
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