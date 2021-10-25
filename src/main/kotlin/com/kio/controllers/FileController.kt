package com.kio.controllers

import com.kio.dto.RenamedEntityDTO
import com.kio.dto.create.CreatedFileDTO
import com.kio.dto.find.FileDTO
import com.kio.services.FileService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/file")
class FileController (val fileService: FileService){

    @PostMapping
    fun save(
        @RequestParam(name = "parent") parentFolderId: String,
        @RequestParam file: MultipartFile,
    ): ResponseEntity<CreatedFileDTO> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(fileService.save(file, parentFolderId))
    }

    @GetMapping(path = ["/{id}"])
    fun findById(@PathVariable(name = "id") fileId: String): ResponseEntity<FileDTO>{
        return ResponseEntity.status(HttpStatus.OK)
            .body(fileService.findById(fileId))
    }

    @PatchMapping(path = ["/{id}/rename"])
    fun rename(
        @PathVariable(name = "id") fileId: String,
        @RequestParam filename: String
    ): ResponseEntity<RenamedEntityDTO>{
        return ResponseEntity.status(HttpStatus.OK)
            .body(fileService.renameFile(fileId, filename))
    }

    @DeleteMapping(path = ["/{id}"])
    fun delete(@PathVariable(name ="id") fileId: String): ResponseEntity<Unit>{
        fileService.deleteFileById(fileId)
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .build()
    }

}