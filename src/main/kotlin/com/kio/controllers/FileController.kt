package com.kio.controllers

import com.kio.dto.ModifyResourceRequest
import com.kio.dto.request.FavoriteRequest
import com.kio.dto.request.file.FileDeleteRequest
import com.kio.dto.request.file.FileUploadRequest
import com.kio.dto.response.FileDTO
import com.kio.services.FileService
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/files")
class FileController (val fileService: FileService){

    @GetMapping("/{id}")
    fun findById(@PathVariable(name = "id") fileId: String): ResponseEntity<FileDTO>{
        return ResponseEntity.status(HttpStatus.OK)
            .body(fileService.findById(fileId))
    }

    @GetMapping("/user/favorites")
    fun findFavorites(): ResponseEntity<Page<FileDTO>> {
        val favorites = fileService.findFavorites()
        return ResponseEntity.status(HttpStatus.OK)
            .body(favorites)
    }

    @PostMapping
    fun save(
        @RequestParam request: FileUploadRequest,
        @RequestPart files: List<MultipartFile>,
        @RequestPart thumbnails: List<MultipartFile>?,
    ): ResponseEntity<Collection<FileDTO>> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(fileService.save(request, files, thumbnails))
    }

    @PatchMapping(path = ["/edit"])
    fun edit(@RequestBody @Valid request: ModifyResourceRequest): ResponseEntity<FileDTO> {
        return ResponseEntity.status(HttpStatus.OK)
            .body(fileService.edit(request))
    }

    @PatchMapping(path = ["/favorite"])
    fun fave(@RequestBody @Valid request: FavoriteRequest): ResponseEntity<Unit> {
        fileService.favorite(request)
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .build()
    }

    @DeleteMapping
    fun delete(@RequestBody deleteRequest: FileDeleteRequest): ResponseEntity<Unit>{
        fileService.deleteAll(deleteRequest)
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .build()
    }

}