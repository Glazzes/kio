package com.kio.controllers

import com.kio.entities.Folder
import com.kio.services.FolderService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/folder")
class FolderController(val folderService: FolderService) {

    @PostMapping
    fun createNewFolder(@RequestParam folderName: String): ResponseEntity<*> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(folderService.save(folderName))
    }

}