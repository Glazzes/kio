package com.kio.services

import com.kio.entities.File
import com.kio.repositories.FileRepository
import com.kio.shared.utils.DiskUtil
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.*
import java.lang.IllegalArgumentException
import javax.transaction.Transactional

@Service
@Transactional
class FileService (val fileRepository: FileRepository){

    @Transactional(rollbackOn = [IOException::class, RuntimeException::class])
    fun save(file: MultipartFile){
        val newFile = File(filename = file.originalFilename, size = file.size)
        DiskUtil.saveFileToDisk(file)
        fileRepository.save(newFile)
    }

    @Transactional(rollbackOn = [FileNotFoundException::class, RuntimeException::class])
    fun deleteFileById(id: String){
        val file = fileRepository.findById(id)
        file.ifPresentOrElse(
            {
                val filename = it?.filename
                    ?: throw IllegalArgumentException("Filename can not be null")

                DiskUtil.deleteFileFromDisk(filename)
                fileRepository.deleteById(id)
            },
            {throw IllegalArgumentException("File with id $id doesn't not exists.") }
        )
    }

}