package com.kio.controllers

import com.kio.dto.request.file.FileDeleteRequest
import com.kio.dto.request.file.FileEditRequest
import com.kio.dto.request.file.FileUploadRequest
import com.kio.dto.response.FileDTO
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
        @RequestParam request: FileUploadRequest,
        @RequestPart files: List<MultipartFile>,
        @RequestPart thumbnails: List<MultipartFile>?,
    ): ResponseEntity<Collection<FileDTO>> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(fileService.save(request, files, thumbnails))
    }

    @GetMapping(path = ["/{id}"])
    fun findById(@PathVariable(name = "id") fileId: String): ResponseEntity<FileDTO>{
        return ResponseEntity.status(HttpStatus.OK)
            .body(fileService.findById(fileId))
    }

    @GetMapping(path = ["/favorites"])
    fun findFavorites(): ResponseEntity<Collection<FileDTO>> {
        val favorites = fileService.findFavorites()
        return ResponseEntity.status(HttpStatus.OK)
            .body(favorites)
    }

    @PatchMapping(path = ["/fave"])
    fun fave(@RequestBody files: Collection<String>): ResponseEntity<Unit> {
        fileService.fave(files)
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .build()
    }

    @PatchMapping(path = ["/{id}/edit"])
    fun edit(@PathVariable id: String, @RequestBody request: FileEditRequest): ResponseEntity<FileDTO> {
        val dto = fileService.edit(id, request)
        return ResponseEntity.status(HttpStatus.OK)
            .body(dto)
    }

    @DeleteMapping
    fun delete(@RequestBody deleteRequest: FileDeleteRequest): ResponseEntity<Unit>{
        fileService.deleteAll(deleteRequest)
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .build()
    }

}