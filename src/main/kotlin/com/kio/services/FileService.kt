package com.kio.services

import com.kio.entities.File
import com.kio.repositories.FileRepository
import com.kio.shared.exception.NotFoundException
import com.kio.shared.utils.DiskUtil
import org.springframework.stereotype.Service
import java.io.*
import java.lang.IllegalArgumentException
import java.time.LocalDate
import java.util.*
import javax.transaction.Transactional

@Service
@Transactional
class FileService (val fileRepository: FileRepository){

    @Transactional(rollbackOn = [IOException::class, RuntimeException::class])
    fun save(file: FileInputStream, filename: String, size: Long){
        Optional.ofNullable(filename)
            .ifPresentOrElse(
                {
                    val newFile = File(filename = filename, size = size, originalFilename = filename)
                    DiskUtil.saveFileToDisk(file, it)
                    fileRepository.save(newFile)
                },
                {throw IllegalArgumentException("Filename must not be null")}
            )
    }

    fun findById(id: String): Optional<File> {
        return fileRepository.findById(id)
    }

    fun renameFile(fileId: String, newFilename: String): File {
        val file = fileRepository.findById(fileId)
            .orElseThrow {throw NotFoundException("Can not rename file with $fileId because it does not exists.")}

        file.apply { filename =  newFilename}
        return file
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

    fun deleteFile(file: File){
        fileRepository.delete(file)
        DiskUtil.deleteFileFromDisk(file.originalFilename)
    }

}