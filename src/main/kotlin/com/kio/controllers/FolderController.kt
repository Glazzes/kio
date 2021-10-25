package com.kio.controllers

import com.kio.dto.create.CreatedFolderDTO
import com.kio.dto.RenamedEntityDTO
import com.kio.services.FolderService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/folder")
class FolderController(val folderService: FolderService) {

    @PostMapping
    fun createFolderInsideOf(
        @RequestParam(name = "parent") parentFolderId: String,
        @RequestParam folderName: String
    ): ResponseEntity<CreatedFolderDTO> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(folderService.save(parentFolderId, folderName))
    }

    @PatchMapping(path = ["/{id}/rename"])
    fun renameFolder(
        @PathVariable(name = "id") folderId: String,
        @RequestParam folderName: String
    ): ResponseEntity<RenamedEntityDTO>{
        return ResponseEntity.status(HttpStatus.OK)
            .body(folderService.rename(folderId, folderName))
    }

    @DeleteMapping(path = ["/{id}"])
    fun deleteFolder(@PathVariable(name = "id") folderId: String): ResponseEntity<Unit> {
        folderService.deleteById(folderId)

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .build()
    }

}