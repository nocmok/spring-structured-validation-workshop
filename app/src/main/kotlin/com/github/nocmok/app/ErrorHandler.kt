package com.github.nocmok.app

import com.github.nocmok.api.model.Error
import com.github.nocmok.api.model.ErrorCode
import com.github.nocmok.api.model.ErrorDetail
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import tools.jackson.databind.DatabindException
import tools.jackson.databind.exc.InvalidFormatException
import tools.jackson.databind.exc.MismatchedInputException
import tools.jackson.databind.exc.ValueInstantiationException
import tools.jackson.databind.ext.javatime.DateTimeParseException

@RestControllerAdvice
class ErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleBeanValidation(e: MethodArgumentNotValidException): Error {
        val details = e
            .bindingResult
            .fieldErrors
            .map { ErrorDetail(it.field).message(it.defaultMessage) }
        return Error(ErrorCode.VALIDATION_ERROR, details).message("invalid request")
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMissingBody(e: HttpMessageNotReadableException): Error {
        val cause = e.cause
        if (cause is DatabindException) {
            return Error(ErrorCode.VALIDATION_ERROR, listOf(getErrorDetail(cause))).message("invalid request")
        }
        return Error(ErrorCode.VALIDATION_ERROR, emptyList()).message("invalid request")
    }

    private fun getErrorDetail(e: DatabindException): ErrorDetail {
        val fieldPathBuilder = StringBuilder()
        for (ref in e.path) {
            if (ref.propertyName != null) {
                if (fieldPathBuilder.isNotEmpty()) fieldPathBuilder.append('.')
                fieldPathBuilder.append(ref.propertyName)
            } else {
                fieldPathBuilder.append('[').append(ref.index).append(']')
            }
        }
        if (e is InvalidFormatException) {
            return ErrorDetail(fieldPathBuilder.toString()).message("invalid value '${e.value}'")
        }
        if (e is DateTimeParseException) {
            return ErrorDetail(fieldPathBuilder.toString()).message("invalid value '${e.value}'")
        }
        return ErrorDetail(fieldPathBuilder.toString()).message("invalid value")
    }

    @ExceptionHandler(Throwable::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleThrowable(): Error {
        return Error(ErrorCode.INTERNAL_ERROR, emptyList()).message("internal error")
    }
}
