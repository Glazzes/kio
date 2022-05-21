package com.kio.services

import com.amazonaws.services.s3.AmazonS3
import com.kio.configuration.properties.BucketConfigurationProperties
import com.kio.dto.response.StaticResponseDTO
import com.kio.entities.File
import com.kio.entities.Folder
import com.kio.entities.enums.FileVisibility
import com.kio.entities.enums.Permission
import com.kio.repositories.FileRepository
import com.kio.repositories.FolderRepository
import com.kio.shared.exception.NotFoundException
import com.kio.shared.utils.PermissionValidatorUtil
import org.springframework.stereotype.Service
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.servlet.http.HttpServletResponse

@Service
class StaticService(
    private val s3: AmazonS3,
    private val fileRepository: FileRepository,
    private val folderRepository: FolderRepository,
    private val buckets: BucketConfigurationProperties
){

    fun downloadFileById(id: String): StaticResponseDTO {
        val file = fileRepository.findById(id)
            .orElseThrow { NotFoundException("File with id $id does not exists") }

        val parentFolder = folderRepository.findById(file.parentFolder)
            .orElseThrow { NotFoundException("Folder with id ${file.parentFolder} does not exists") }

        if(file.visibility != FileVisibility.PUBLIC) {
            PermissionValidatorUtil.checkFolderPermissions(parentFolder, Permission.READ_ONLY)
        }

        val content = s3.getObject(buckets.filesBucket, file.bucketKey)
            .objectContent

        return StaticResponseDTO(file.contentType, content)
    }

    fun downloadFolderById(id: String, response: HttpServletResponse) {
        val folder = folderRepository.findById(id)
            .orElseThrow { NotFoundException("Could not found folder with id $id") }

        PermissionValidatorUtil.checkFolderPermissions(folder, Permission.READ_ONLY)

        response.setHeader("Content-Disposition", "attachment;filename=${folder.name}.zip")

        val zos = ZipOutputStream(response.outputStream)
        this.zipFoldersRecursively("", folder, zos)
        zos.close()
    }

    private fun zipFoldersRecursively(rootName: String, folder: Folder, zos: ZipOutputStream) {
        val subFolders = folderRepository.findByIdIsIn(folder.subFolders)
        val files = fileRepository.findByIdIsIn(folder.files)

        val currentFolderName = "${rootName}/${folder.name}"
        files.forEach { this.writeS3ObjectToZip(currentFolderName, it, zos) }

        for(subFolder in subFolders) {
            this.zipFoldersRecursively(currentFolderName, subFolder, zos)
        }
    }

    private fun writeS3ObjectToZip(rootName: String, file: File, zos: ZipOutputStream) {
        val content = s3.getObject(buckets.filesBucket, file.bucketKey)
            .objectContent
            .delegateStream

        content.use {
            val entry = ZipEntry("${rootName}/${file.name}")
            zos.putNextEntry(entry)

            // I have tried many values but only 4 and 8 will not turn out in a "corrupted" file
            val bytes = ByteArray(8)
            while (it.read(bytes) != -1) {
                zos.write(bytes)
            }

            zos.closeEntry()
        }
    }

}