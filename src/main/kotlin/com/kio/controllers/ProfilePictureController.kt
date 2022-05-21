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

@RestController
@RequestMapping("/api/v1/pfp")
class ProfilePictureController(
    private val profilePictureService: ProfilePictureService
){

    @GetMapping(path = ["/default"])
    fun findDefault(): ResponseEntity<InputStreamResource> {
        val static = profilePictureService.findDefault()
        return ResponseEntity.status(HttpStatus.OK)
            .contentType(MediaType.valueOf(static.contentType))
            .body(InputStreamResource(static.inputStream))
    }

    @GetMapping(path = ["/me"])
    fun findMine(): ResponseEntity<InputStreamResource> {
        val static = profilePictureService.findMine()
        return ResponseEntity.status(HttpStatus.OK)
            .contentType(MediaType.valueOf(static.contentType))
            .body(InputStreamResource(static.inputStream))
    }

    @PostMapping(path = ["/me"])
    fun set(@RequestPart file: MultipartFile): ResponseEntity<InputStreamResource> {
        val static = profilePictureService.findMine()
        return ResponseEntity.status(HttpStatus.OK)
            .contentType(MediaType.valueOf(static.contentType))
            .body(InputStreamResource(static.inputStream))
    }

    @GetMapping(path = ["/{id}"])
    fun findById(@PathVariable id: String): ResponseEntity<InputStreamResource> {
        val static = profilePictureService.findById(id)
        return ResponseEntity.status(HttpStatus.OK)
            .contentType(MediaType.valueOf(static.contentType))
            .body(InputStreamResource(static.inputStream))
    }

    @GetMapping(path = ["/user/{id}"])
    fun findByUserId(@PathVariable id: String): ResponseEntity<InputStreamResource> {
        val static = profilePictureService.findByUserId(id)
        return ResponseEntity.status(HttpStatus.OK)
            .contentType(MediaType.valueOf(static.contentType))
            .body(InputStreamResource(static.inputStream))
    }

}