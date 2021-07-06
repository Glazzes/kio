package com.kio.shared.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.io.FileNotFoundException
import java.time.LocalDateTime

@RestControllerAdvice
class ExceptionHandler {

    @ExceptionHandler(value = [BadRequestException::class])
    fun handleBadRequestException(e: BadRequestException): ResponseEntity<ExceptionDetails>{
        val details = ExceptionDetails(e.message, LocalDateTime.now())

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(details)
    }

    @ExceptionHandler(value = [NotFoundException::class, FileNotFoundException::class])
    fun handleUserNotFoundException(e: Exception): ResponseEntity<ExceptionDetails>{
        val details = ExceptionDetails(e.message, LocalDateTime.now())

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(details)
    }

    data class ExceptionDetails(
        val causedBy: String?,
        val throwAt: LocalDateTime,
    )

}