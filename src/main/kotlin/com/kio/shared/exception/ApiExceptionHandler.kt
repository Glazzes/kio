package com.kio.shared.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.validation.BindException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.io.FileNotFoundException
import java.lang.UnsupportedOperationException
import java.time.LocalDateTime
import javax.servlet.http.HttpServletRequest
import javax.xml.bind.DataBindingException

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(value = [BadRequestException::class])
    fun handleBadRequestException(e: BadRequestException, request: HttpServletRequest): ResponseEntity<ExceptionDetails>{
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ExceptionDetails(e.message, LocalDateTime.now(), request.servletPath))
    }

    @ExceptionHandler(value = [BindException::class])
    fun handleBindException(e: BindException): ResponseEntity<Map<String, String?>> {
        val errors = e.bindingResult.fieldErrors.associate { it.field to it.defaultMessage }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(errors)
    }

    @ExceptionHandler(value = [NotFoundException::class, FileNotFoundException::class])
    fun handleUserNotFoundException(e: Exception, request: HttpServletRequest): ResponseEntity<ExceptionDetails>{
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ExceptionDetails(e.message, LocalDateTime.now(), request.servletPath))
    }

    @ExceptionHandler(value = [
        UnsupportedOperationException::class,
        IllegalAccessException::class,
        IllegalOperationException::class
    ])
    fun handleUnsupportedOperationException(e: Exception, request: HttpServletRequest): ResponseEntity<ExceptionDetails> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ExceptionDetails(e.message, LocalDateTime.now(), request.servletPath))
    }

    @ExceptionHandler(value = [InvalidTokenException::class, BadCredentialsException::class])
    fun handleUnAuthozired(e: Exception, request: HttpServletRequest): ResponseEntity<ExceptionDetails> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ExceptionDetails(e.message, LocalDateTime.now(), request.servletPath))
    }

    @ExceptionHandler(value = [
        AlreadyExistsException::class,
        FileTreeException::class,
        InsufficientStorageException::class
    ])
    fun handleConflicts(e: Exception, request: HttpServletRequest): ResponseEntity<ExceptionDetails> {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ExceptionDetails(e.message, LocalDateTime.now(), request.servletPath))
    }

    data class ExceptionDetails(
        val cause: String?,
        val timeStamp: LocalDateTime,
        val path: String,
    )

}