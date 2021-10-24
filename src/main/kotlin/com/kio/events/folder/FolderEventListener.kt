package com.kio.events.folder

import com.kio.entities.Folder
import com.kio.services.FileService
import com.kio.services.FolderService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class FolderEventListener {
    @Autowired private lateinit var fileService: FileService
    @Autowired private lateinit var folderService: FolderService

    @EventListener(value = [FolderDeleteEvent::class])
    fun onFolderDeleteEvent(event: FolderDeleteEvent){
        deleteFolder(event.folder)
    }

    private fun deleteFolder(folder: Folder){
        for (file in folder.files) fileService.deleteFile(file)
        for(subFolder in folder.subFolders) deleteFolder(subFolder)
        folderService.deleteFolder(folder)
    }

}