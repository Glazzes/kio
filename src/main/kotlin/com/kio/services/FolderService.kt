package com.kio.services

import com.kio.entities.Folder
import com.kio.repositories.FolderRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import javax.transaction.Transactional

@Service
@Transactional
class FolderService(val folderRepository: FolderRepository){

    fun saveNewFolder(folderName: String): Folder{
        val newFolder = Folder(folderName = folderName, createdAt = LocalDate.now())
        return folderRepository.save(newFolder)
    }

}