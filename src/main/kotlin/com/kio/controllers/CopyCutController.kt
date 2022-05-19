package com.kio.controllers

import com.kio.dto.request.file.FileCopyRequest
import com.kio.dto.request.folder.FolderCopyRequest
import com.kio.services.CopyService
import com.kio.services.CutService
import com.kio.shared.utils.ControllerUtil
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/cc")
class CopyCutController(
    private val copyService: CopyService,
    private val cutService: CutService
){

    @PutMapping("/folder/cut")
    fun cutFolder(@RequestBody @Valid request: FolderCopyRequest, bindingResult: BindingResult): ResponseEntity<*> {
        if(bindingResult.hasFieldErrors()){
            val fieldErrors = ControllerUtil.getRequestErrors(bindingResult)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(fieldErrors)
        }

        val dto = cutService.cutFolder(request)
        return ResponseEntity.status(HttpStatus.OK)
            .body(dto)
    }

    @PutMapping(path = ["/files/copy"])
    fun copyFiles(@RequestBody @Valid request: FileCopyRequest, bindingResult: BindingResult): ResponseEntity<*> {
        if(bindingResult.hasFieldErrors()){
            val fieldErrors = ControllerUtil.getRequestErrors(bindingResult)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(fieldErrors)
        }

        val cutFiles = copyService.copyFiles(request)
        return ResponseEntity.status(HttpStatus.OK)
            .body(cutFiles)
    }

    @PutMapping(path = ["/files/cut"])
    fun cutFiles(@RequestBody @Valid request: FileCopyRequest, bindingResult: BindingResult): ResponseEntity<*> {
        if(bindingResult.hasFieldErrors()){
            val fieldErrors = ControllerUtil.getRequestErrors(bindingResult)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(fieldErrors)
        }

        val cutFiles = cutService.cutFiles(request)
        return ResponseEntity.status(HttpStatus.OK)
            .body(cutFiles)
    }

}