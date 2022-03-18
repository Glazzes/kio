package com.kio.controllers

import com.kio.dto.response.save.SavedFolderDTO
import com.kio.services.FolderService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/folder")
class FolderController(val folderService: FolderService) {

    @GetMapping(path = ["/{id}"])
    fun get(@PathVariable id: String): ResponseEntity<Void> {
        return ResponseEntity.status(HttpStatus.OK)
            .build()
    }

    @PostMapping(path = ["/{id}/create"])
    fun create(
        @PathVariable("id") parentFolderId: String,
        @RequestParam name: String
    ): ResponseEntity<SavedFolderDTO> {
        val createdFolderDTO = folderService.save(parentFolderId, name)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(createdFolderDTO)
    }

}