package com.kio.controllers

import com.kio.dto.RenamedEntityDTO
import com.kio.services.FileService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.FileInputStream

@RestController
@RequestMapping("/files")
class FileController (val fileService: FileService){

    @PostMapping
    fun saveNewFile(@RequestPart file: MultipartFile): ResponseEntity<Unit> {
        fileService.save(
            (file.inputStream as FileInputStream),
            file.originalFilename,
            file.size
        )

        return ResponseEntity.status(HttpStatus.CREATED)
            .build()
    }

    @PatchMapping(path = ["/{id}/rename"])
    fun renameFile(
        @PathVariable(name = "id") fileId: String,
        @RequestParam filename: String
    ): ResponseEntity<RenamedEntityDTO>{
        val file = fileService.renameFile(fileId, filename)
        return ResponseEntity.status(HttpStatus.OK)
            .body(RenamedEntityDTO(file.filename, file.lastModified))
    }

}