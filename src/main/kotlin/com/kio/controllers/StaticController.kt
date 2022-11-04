package com.kio.controllers

import com.kio.services.StaticService
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/static")
class StaticController(private val staticService: StaticService){

    @GetMapping(path = ["/file/{id}"])
    fun downloadFileById(@PathVariable id: String): ResponseEntity<InputStreamResource> {
        val dto = staticService.downloadFileById(id)
        val fileToDownload = dto.inputStream

        return ResponseEntity.status(HttpStatus.OK)
            .contentType(MediaType.valueOf(dto.contentType))
            .body(InputStreamResource(fileToDownload))
    }

    @GetMapping(path = ["/file/{id}/thumbnail"])
    fun downloadThumbnailByFileId(@PathVariable id: String): ResponseEntity<InputStreamResource> {
        val dto = staticService.downloadThumbnail(id)
        val fileToDownload = dto.inputStream

        return ResponseEntity.status(HttpStatus.OK)
            .contentType(MediaType.valueOf(dto.contentType))
            .body(InputStreamResource(fileToDownload))
    }

    @GetMapping(path = ["/folder/{id}"])
    fun downloadFolderById(@PathVariable id: String, response: HttpServletResponse): ResponseEntity<Unit> {
        staticService.downloadFolderById(id, response)
        return ResponseEntity.status(HttpStatus.OK)
            .build()
    }

}