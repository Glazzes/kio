package com.kio.controllers

import com.kio.dto.response.find.FolderDTO
import com.kio.dto.response.save.SavedFolderDTO
import com.kio.entities.enums.FileState
import com.kio.services.FolderService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/folders")
class FolderController(val folderService: FolderService) {

    @PostMapping(path = ["/{id}/create"])
    fun create(
        @PathVariable("id") parentFolderId: String,
        @RequestParam name: String
    ): ResponseEntity<SavedFolderDTO> {
        val createdFolderDTO = folderService.save(parentFolderId, name)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(createdFolderDTO)
    }

    @GetMapping(path = ["/my-unit"])
    fun findAuthenticatedUserUnit(): ResponseEntity<FolderDTO> {
        return ResponseEntity.status(HttpStatus.OK)
            .body(folderService.findCurrentUserUnit())
    }

    @GetMapping(path = ["/{id}"])
    fun findById(@PathVariable id: String): ResponseEntity<FolderDTO> {
        return ResponseEntity.status(HttpStatus.OK)
            .body(folderService.findById(id))
    }

    @GetMapping(path = ["/{id}/subFolders"])
    fun findSubFoldersById(@PathVariable id: String): ResponseEntity<Collection<FolderDTO>> {
        return ResponseEntity.status(HttpStatus.OK)
            .body(folderService.findSubFoldersByParentId(id))
    }

    @PatchMapping(path = ["/{id}/state"])
    fun modifyState(@PathVariable id: String, @RequestParam("new-state") newState: FileState): ResponseEntity<Unit> {
        folderService.modifyState(id, newState)
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .build()
    }

}