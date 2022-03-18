package com.kio.shared.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import java.io.FileNotFoundException
import java.lang.UnsupportedOperationException
import java.time.LocalDateTime

@RestControllerAdvice
class ApiExceptionHandler {

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

    @ExceptionHandler(value = [UnsupportedOperationException::class])
    fun handleUnsupportedOperationException(e: Exception): ResponseEntity<ExceptionDetails> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ExceptionDetails(e.message, LocalDateTime.now()))
    }

    data class ExceptionDetails(
        val causedBy: String?,
        val throwAt: LocalDateTime,
    )

}