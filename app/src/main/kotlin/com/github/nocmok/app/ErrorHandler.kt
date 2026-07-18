package com.github.nocmok.app

import com.github.nocmok.api.model.Error
import com.github.nocmok.api.model.ErrorCode
import com.github.nocmok.api.model.ErrorDetail
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleBeanValidation(e: MethodArgumentNotValidException): Error {
        val details = e
            .bindingResult
            .fieldErrors
            .map {
                ErrorDetail(
                    field = it.field,
                    message = it.defaultMessage,
                )
            }
        val error = Error(
            code = ErrorCode.VALIDATION_ERROR,
            message = "Invalid request",
            details = details,
        )
        return error
    }
}
