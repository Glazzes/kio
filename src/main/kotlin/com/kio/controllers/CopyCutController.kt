package com.kio.controllers

import com.kio.dto.request.file.FileCopyRequest
import com.kio.dto.response.FileDTO
import com.kio.dto.response.FolderDTO
import com.kio.services.CopyService
import com.kio.services.CutService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/cc")
class CopyCutController(
    private val copyService: CopyService,
    private val cutService: CutService
){

    @PutMapping("/folders/copy")
    fun copyFolders(@RequestBody @Valid request: FileCopyRequest): ResponseEntity<Collection<FolderDTO>> {
        return ResponseEntity.status(HttpStatus.OK)
            .body(copyService.copyFolders(request))
    }

    @PutMapping("/folders/cut")
    fun cutFolders(@RequestBody @Valid request: FileCopyRequest): ResponseEntity<Collection<FolderDTO>> {
        return ResponseEntity.status(HttpStatus.OK)
            .body(cutService.cutFolders(request))
    }

    @PutMapping(path = ["/files/copy"])
    fun copyFiles(@RequestBody @Valid request: FileCopyRequest): ResponseEntity<*> {
        val cutFiles = copyService.copyFiles(request)
        return ResponseEntity.status(HttpStatus.OK)
            .body(cutFiles)
    }

    @PutMapping(path = ["/files/cut"])
    fun cutFiles(@RequestBody @Valid request: FileCopyRequest): ResponseEntity<Collection<FileDTO>> {
        val cutFiles = cutService.cutFiles(request)
        return ResponseEntity.status(HttpStatus.OK)
            .body(cutFiles)
    }

}