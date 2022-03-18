package com.kio.controllers

import com.kio.dto.response.find.FileDTO
import com.kio.dto.response.modify.RenamedEntityDTO
import com.kio.dto.response.save.SavedFileDTO
import com.kio.dto.request.GenericResourceRequest
import com.kio.services.FileService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/files")
class FileController (val fileService: FileService){

    @PostMapping
    fun save(
        @RequestParam(name = "parent") parentFolderId: String,
        @RequestParam file: MultipartFile,
    ): ResponseEntity<SavedFileDTO> {
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
            .body(fileService.rename(fileId, filename))
    }

    @DeleteMapping(path = ["/{id}"])
    fun delete(@PathVariable id: String): ResponseEntity<Unit>{
        fileService.deleteById(id)
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .build()
    }

    @DeleteMapping(path = ["/many"])
    fun deleteMany(@RequestBody deleteRequest: GenericResourceRequest) : ResponseEntity<Unit> {
        fileService.deleteMany(deleteRequest)
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .build()
    }

}