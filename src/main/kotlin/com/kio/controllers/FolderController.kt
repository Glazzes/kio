package com.kio.controllers

import com.kio.dto.ModifyResourceRequest
import com.kio.dto.response.FileDTO
import com.kio.dto.response.FolderDTO
import com.kio.dto.response.UnitSizeDTO
import com.kio.services.FolderService
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

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
    fun save(@PathVariable id: String, @RequestParam name: String): ResponseEntity<FolderDTO> {
        val createdFolderDTO = folderService.save(id, name)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(createdFolderDTO)
    }

    @PatchMapping(path = ["/edit"])
    fun edit(@RequestBody @Valid request: ModifyResourceRequest): ResponseEntity<FolderDTO> {
        return ResponseEntity.status(HttpStatus.OK)
            .body(folderService.edit(request))
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

    @GetMapping("/unit/size")
    fun findUnitSize(): ResponseEntity<UnitSizeDTO> {
        return ResponseEntity.status(HttpStatus.OK)
            .body(folderService.findUnitSize())
    }

    @GetMapping(path = ["/{id}/size"])
    fun findFolderSize(@PathVariable id: String): ResponseEntity<Long> {
        return ResponseEntity.status(HttpStatus.OK)
            .body(folderService.findFolderSizeById(id))
    }

    @GetMapping(path = ["/favorites"])
    fun findFavorites(): ResponseEntity<Collection<FolderDTO>> {
        val favorites = folderService.findFavorites()
        return ResponseEntity.status(HttpStatus.OK)
            .body(favorites)
    }

    @PatchMapping(path = ["/fave"])
    fun fave(@RequestBody folders: Collection<String>): ResponseEntity<Unit> {
        folderService.fave(folders)
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .build()
    }

    @DeleteMapping(path = ["/{id}"])
    fun delete(@PathVariable id: String): ResponseEntity<Unit> {
        folderService.deleteFolder(id)
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .build()
    }

}