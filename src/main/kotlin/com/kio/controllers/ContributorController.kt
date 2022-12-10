package com.kio.controllers

import com.kio.dto.request.contributor.ContributorDeleteRequest
import com.kio.dto.request.contributor.ContributorUpdatePermissionsRequest
import com.kio.dto.request.contributor.ContributorAddRequest
import com.kio.dto.response.UserDTO
import com.kio.services.ContributorService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
class ContributorController(private val contributorService: ContributorService) {
    @PostMapping("/api/v1/folder/contributors")
    fun save(@RequestBody @Valid contributorRequest: ContributorAddRequest): ResponseEntity<Collection<UserDTO>> {
        val contributorDTO = contributorService.save(contributorRequest)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(contributorDTO)
    }

    @GetMapping("/api/v1/folders/{id}/contributors")
    fun findFolderContributors(@PathVariable id: String): ResponseEntity<Collection<UserDTO>> {
        return ResponseEntity.status(HttpStatus.OK)
            .body(contributorService.findFolderContributors(id))
    }

    @PatchMapping("/api/v1/folder/contributors")
    fun updatePermissions(@RequestBody @Valid request: ContributorUpdatePermissionsRequest): ResponseEntity<Unit> {
        contributorService.update(request)
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .build()
    }

    @DeleteMapping("/api/v1/folder/contributors")
    fun delete(@RequestBody @Valid request: ContributorDeleteRequest): ResponseEntity<Unit> {
        contributorService.delete(request)
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .build()
    }

    @DeleteMapping("/api/v1/folder/{id}/contributors")
    fun deleteContributorByThemselves(@PathVariable id: String): ResponseEntity<Unit> {
        contributorService.deleteContribtorByThemselves(id)
        return ResponseEntity.status(HttpStatus.OK)
            .build()
    }

}