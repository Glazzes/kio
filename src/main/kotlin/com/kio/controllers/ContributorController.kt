package com.kio.controllers

import com.kio.dto.ContributorInfo
import com.kio.dto.request.NewContributorRequest
import com.kio.services.ContributorService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/contributors")
class ContributorController(private val contributorService: ContributorService) {

    @PostMapping(path = ["/folder/{id}"])
    fun saveContributor(
        @PathVariable id: String,
        @RequestBody contributorRequest: NewContributorRequest,
    ): ResponseEntity<ContributorInfo> {
        val contributorDTO = contributorService.save(id, contributorRequest)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(contributorDTO)
    }

}