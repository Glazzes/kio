package com.kio.controllers

import com.kio.dto.request.ShareRequest
import com.kio.services.SharingService
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody

@RestController
@RequestMapping("/api/v1/sharing")
class SharingController(
    private val sharingService: SharingService
){

    @PostMapping(path = ["/file"])
    fun register(@RequestBody shareRequest: ShareRequest): ResponseEntity<String> {
        val link = sharingService.shareFile(shareRequest)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(link)
    }

    @GetMapping(path = ["/file/{id}"])
    fun findById(@PathVariable id: String): ResponseEntity<StreamingResponseBody> {
        val static = sharingService.findById(id)
        return ResponseEntity.status(HttpStatus.OK)
            .contentType(MediaType.valueOf(static.contentType))
            .body(static.responseBody)
    }

}