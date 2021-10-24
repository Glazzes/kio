package com.kio.services

import com.kio.entities.Folder
import com.kio.events.folder.FolderApplicationPublisher
import com.kio.events.folder.FolderDeleteEvent
import com.kio.repositories.FolderRepository
import com.kio.shared.utils.DiskUtil
import javassist.NotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
@Transactional
class FolderService{
    @Autowired private lateinit var folderRepository: FolderRepository
    @Autowired private lateinit var folderEventPublisher: FolderApplicationPublisher

    fun save(folderName: String): Folder{
        val newFolder = Folder(folderName = folderName, originalFolderName = folderName)
        return folderRepository.save(newFolder)
    }

    fun renameFolder(folderId: String, newFolderName: String): Folder{
        val folder = folderRepository.findById(folderId)
            .orElseThrow {throw NotFoundException("Can not rename folder $folderId because it does not exists")}

        folder.apply { folderName = newFolderName }
        return folder
    }

    fun deleteFolderById(folderId: String){
        folderRepository.findById(folderId)
            .ifPresentOrElse(
                { folderEventPublisher.multicastEvent(FolderDeleteEvent(it)) },
                {throw NotFoundException("Folder with id $folderId can not be deleted because it does not exists")}
            )
    }

    fun deleteFolder(folder: Folder){
        folderRepository.delete(folder)
        DiskUtil.deleteFolderFromDisk(folder.originalFolderName)
    }

}