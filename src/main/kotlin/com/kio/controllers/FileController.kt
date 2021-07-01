package com.kio.controllers

import com.kio.services.FileService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/files")
class FileController (val fileService: FileService){

    @PostMapping
    fun saveNewFile(@RequestPart file: MultipartFile): ResponseEntity<Unit> {
        fileService.save(file)
        return ResponseEntity.status(HttpStatus.CREATED)
            .build()
    }

}