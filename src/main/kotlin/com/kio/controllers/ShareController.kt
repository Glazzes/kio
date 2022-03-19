package com.kio.controllers

import com.kio.dto.request.ShareRequest
import com.kio.services.ShareService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/share")
class ShareController(private val shareService: ShareService) {

    @PostMapping(path = ["/file"])
    fun shareFile(shareRequest: ShareRequest): ResponseEntity<Unit> {
        shareService.shareFile(shareRequest)

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .build()
    }

    @PostMapping(path = ["/folder"])
    fun shareFolder(shareRequest: ShareRequest): ResponseEntity<Unit> {
        shareService.shareFile(shareRequest)

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .build()
    }

}