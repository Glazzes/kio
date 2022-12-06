package com.kio.controllers

import com.kio.services.ProfilePictureService
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody

@RestController
@RequestMapping("/api/v1/pfp")
class ProfilePictureController(
    private val profilePictureService: ProfilePictureService
){

    @GetMapping(path = ["/{userId}/{id}"])
    fun findByUserId(@PathVariable userId: String, @PathVariable id: String): ResponseEntity<StreamingResponseBody> {
        val static = profilePictureService.findByUserId(userId, id)
        return ResponseEntity.status(HttpStatus.OK)
            .contentType(MediaType.valueOf(static.contentType))
            .body(static.responseBody)
    }

}