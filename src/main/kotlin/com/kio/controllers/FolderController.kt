package com.kio.controllers

import com.kio.dto.RenamedEntityDTO
import com.kio.services.FolderService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/folder")
class FolderController(val folderService: FolderService) {

    @PostMapping
    fun createFolder(@RequestParam folderName: String): ResponseEntity<*> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(folderService.save(folderName))
    }

    @PatchMapping(path = ["/{id}/rename"])
    fun renameFolder(
        @PathVariable(name = "id") folderId: String,
        @RequestParam folderName: String
    ): ResponseEntity<RenamedEntityDTO>{
        val renamedFolder = folderService.renameFolder(folderId, folderName)

        return ResponseEntity.status(HttpStatus.OK)
            .body(RenamedEntityDTO(renamedFolder.folderName, renamedFolder.lastModified))
    }

    @DeleteMapping(path = ["/{id}"])
    fun deleteFolder(@PathVariable(name = "id") folderId: String): ResponseEntity<Unit> {
        folderService.deleteFolderById(folderId)

        return ResponseEntity.status(HttpStatus.OK)
            .build()
    }

}