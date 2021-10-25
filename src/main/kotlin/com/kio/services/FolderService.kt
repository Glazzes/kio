package com.kio.services

import com.kio.dto.create.CreatedFolderDTO
import com.kio.dto.RenamedEntityDTO
import com.kio.entities.Folder
import com.kio.events.folder.FolderApplicationPublisher
import com.kio.events.folder.FolderDeleteEvent
import com.kio.repositories.FolderRepository
import com.kio.shared.utils.DiskUtil
import javassist.NotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

@Service
@Transactional
class FolderService{
    @Autowired private lateinit var folderRepository: FolderRepository
    @Autowired private lateinit var folderEventPublisher: FolderApplicationPublisher

    fun save(parentFolderId: String, folderName: String): CreatedFolderDTO {
        val newFolder = Folder(folderName = folderName, originalFolderName = folderName)
        val createdFolder = folderRepository.save(newFolder)

        val parentFolder = folderRepository.findById(parentFolderId)
            .orElseThrow { NotFoundException("""
            Can not create folder because the parent folder with id $parentFolderId does not exists    
            """.trimIndent()) }

        parentFolder.apply { subFolders.add(createdFolder) }

        return CreatedFolderDTO(
            createdFolder.id    ,
            createdFolder.folderName,
            createdFolder.spaceUsed,
            createdFolder.lastModified
        )
    }

    fun findById(id: String): Optional<Folder> {
        return folderRepository.findById(id)
    }

    fun rename(folderId: String, newFolderName: String): RenamedEntityDTO{
        val folder = folderRepository.findById(folderId)
            .orElseThrow {throw NotFoundException("Can not rename folder $folderId because it does not exists")}

        folder.apply { folderName = newFolderName }
        return RenamedEntityDTO(folder.folderName, folder.lastModified)
    }

    fun deleteById(folderId: String){
        folderRepository.findById(folderId)
            .ifPresentOrElse(
                { folderEventPublisher.multicastEvent(FolderDeleteEvent(it)) },
                {throw NotFoundException("Folder with id $folderId can not be deleted because it does not exists")}
            )
    }

    fun delete(folder: Folder){
        folderRepository.delete(folder)
        DiskUtil.deleteFolderFromDisk(folder.originalFolderName)
    }

}