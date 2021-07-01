package com.kio.shared.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.io.FileNotFoundException
import java.time.LocalDate

@RestControllerAdvice
class ExceptionHandler {

    @ExceptionHandler(value = [BadRequestException::class])
    fun handleBadRequestException(e: BadRequestException): ResponseEntity<ExceptionDetails>{
        val details = ExceptionDetails(e.message, LocalDate.now())

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(details)
    }

    @ExceptionHandler(value = [UserNotFoundException::class])
    fun handleUserNotFoundException(): ResponseEntity<Unit>{
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .build()
    }

    @ExceptionHandler(value = [FileNotFoundException::class])
    fun handleFileNotFoundException(e: FileNotFoundException): ResponseEntity<ExceptionDetails> {
        val details = ExceptionDetails(e.message, LocalDate.now())
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(details)
    }

    data class ExceptionDetails(
        val causedBy: String?,
        val throwAt: LocalDate,
    )

}