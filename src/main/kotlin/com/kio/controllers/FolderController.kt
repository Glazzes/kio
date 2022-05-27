package com.kio.controllers

import com.kio.dto.request.folder.FolderCreateRequest
import com.kio.dto.request.folder.FolderEditRequest
import com.kio.dto.response.FileDTO
import com.kio.dto.response.FolderDTO
import com.kio.services.FolderService
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/folders")
class FolderController(private val folderService: FolderService) {

    @GetMapping(path = ["/my-unit"])
    fun findAuthenticatedUserUnit(): ResponseEntity<FolderDTO> {
        return ResponseEntity.status(HttpStatus.OK)
            .body(folderService.findAuthenticatedUserUnit())
    }

    @GetMapping("/shared")
    fun findSharedFolders(): ResponseEntity<Collection<FolderDTO>> {
        return ResponseEntity.status(HttpStatus.OK)
            .body(folderService.findAuthenticatedUserSharedFolders())
    }

    @GetMapping(path = ["/{id}"])
    fun findById(@PathVariable id: String): ResponseEntity<FolderDTO> {
        return ResponseEntity.status(HttpStatus.OK)
            .body(folderService.findById(id))
    }

    @PostMapping(path = ["/{id}"])
    fun save(@PathVariable id: String, @RequestBody request: FolderCreateRequest): ResponseEntity<FolderDTO> {
        val createdFolderDTO = folderService.save(id, request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(createdFolderDTO)
    }

    @GetMapping(path = ["/{id}/sub-folders"])
    fun findSubFoldersById(@PathVariable id: String, @RequestParam page: Int): ResponseEntity<Page<FolderDTO>> {
        return ResponseEntity.status(HttpStatus.OK)
            .body(folderService.findSubFoldersByParentId(id, page))
    }

    @GetMapping(path = ["/{id}/files"])
    fun findFilesByFolderId(@PathVariable id: String, @RequestParam page: Int): ResponseEntity<Page<FileDTO>> {
        return ResponseEntity.status(HttpStatus.OK)
            .body(folderService.findFilesByFolderId(id, page))
    }

    @GetMapping(path = ["/{id}/size"])
    fun findFolderSize(@PathVariable id: String): ResponseEntity<Long> {
        return ResponseEntity.status(HttpStatus.OK)
            .body(folderService.findFolderSizeById(id))
    }

    @PatchMapping(path = ["/{id}/edit"])
    fun edit(@PathVariable id: String, @RequestBody request: FolderEditRequest): ResponseEntity<FolderDTO> {
        val dto = folderService.edit(id, request)
        return ResponseEntity.status(HttpStatus.OK)
            .body(dto)
    }

    @DeleteMapping(path = ["/{id}"])
    fun delete(@PathVariable id: String, @RequestBody subFolders: Collection<String>): ResponseEntity<Unit> {
        folderService.deleteAll(id, subFolders)
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .build()
    }

}