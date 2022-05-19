package com.kio.controllers

import com.kio.dto.request.contributor.ContributorDeleteRequest
import com.kio.dto.request.contributor.ContributorUpdatePermissionsRequest
import com.kio.dto.request.contributor.ContributorAddRequest
import com.kio.services.ContributorService
import com.kio.shared.utils.ControllerUtil
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/contributors")
class ContributorController(private val contributorService: ContributorService) {

    @PostMapping(path = ["/folder/{id}"])
    fun save(
        @PathVariable id: String,
        @RequestBody @Valid contributorRequest: ContributorAddRequest,
        bindingResult: BindingResult
    ): ResponseEntity<*> {
        if(bindingResult.hasFieldErrors()){
            val fieldErrors = ControllerUtil.getRequestErrors(bindingResult)

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(fieldErrors)
        }

        val contributorDTO = contributorService.save(id, contributorRequest)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(contributorDTO)
    }

    @PatchMapping
    fun updatePermissions(@RequestBody request: ContributorUpdatePermissionsRequest): ResponseEntity<Unit> {
        contributorService.update(request)
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .build()
    }

    @DeleteMapping
    fun delete(@RequestBody @Valid request: ContributorDeleteRequest): ResponseEntity<Unit> {
        contributorService.delete(request)
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .build()
    }

}