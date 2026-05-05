package com.waytofit.global.error

import com.waytofit.global.common.response.ApiResponse
import com.waytofit.global.common.response.ResponseCode
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class GlobalExceptionHandler(
    private val environment: Environment
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val isLocalProfile: Boolean
        get() = environment.activeProfiles.contains("local")

    /**
     * 비즈니스 예외 처리
     */
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<ApiResponse<Unit>> {
        if (isLocalProfile) {
            log.warn(
                "BusinessException occurred at {}: [Code: {}, Message: {}]",
                findExceptionOrigin(e),
                e.responseCode.code,
                e.message,
                e
            )
        } else {
            log.warn("BusinessException occurred: [Code: {}, Message: {}]", e.responseCode.code, e.message)
        }
        val response = ApiResponse.error<Unit>(e.responseCode, e.overrideMessage)
        return ResponseEntity.status(e.responseCode.httpStatus).body(response)
    }

    /**
     * @Valid 검증 실패 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Unit>> {
        val errorMessage = e.bindingResult.fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage}" }.ifEmpty { ResponseCode.INVALID_REQUEST_BODY.message }
        if (isLocalProfile) {
            log.warn(
                "ValidationException occurred at {}: {}",
                findExceptionOrigin(e),
                errorMessage,
                e
            )
        } else {
            log.warn("ValidationException occurred: {}", errorMessage)
        }
        val response = ApiResponse.error<Unit>(ResponseCode.INVALID_REQUEST_BODY, errorMessage)
        return ResponseEntity.status(ResponseCode.INVALID_REQUEST_BODY.httpStatus).body(response)
    }

    /**
     * 쿼리 파라미터 타입 불일치 예외 처리
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatchException(e: MethodArgumentTypeMismatchException): ResponseEntity<ApiResponse<Unit>> {
        val errorMessage = "파라미터 '${e.name}'의 값이 잘못되었습니다. (기대 타입: ${e.requiredType?.simpleName})"
        if (isLocalProfile) {
            log.warn("MethodArgumentTypeMismatchException occurred at {}: {}", findExceptionOrigin(e), errorMessage, e)
        } else {
            log.warn("MethodArgumentTypeMismatchException occurred: {}", errorMessage)
        }
        val response = ApiResponse.error<Unit>(ResponseCode.INVALID_PARAMETER, errorMessage)
        return ResponseEntity.status(ResponseCode.INVALID_PARAMETER.httpStatus).body(response)
    }

    /**
     * 바인딩 예외 처리 (쿼리 파라미터 등)
     */
    @ExceptionHandler(BindException::class)
    fun handleBindException(e: BindException): ResponseEntity<ApiResponse<Unit>> {
        val errorMessage = e.bindingResult.fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage}" }.ifEmpty { ResponseCode.INVALID_PARAMETER.message }
        if (isLocalProfile) {
            log.warn("BindException occurred at {}: {}", findExceptionOrigin(e), errorMessage, e)
        } else {
            log.warn("BindException occurred: {}", errorMessage)
        }
        val response = ApiResponse.error<Unit>(ResponseCode.INVALID_PARAMETER, errorMessage)
        return ResponseEntity.status(ResponseCode.INVALID_PARAMETER.httpStatus).body(response)
    }

    /**
     * 리소스를 찾을 수 없는 경우 (404)
     */
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException::class)
    fun handleNoResourceFoundException(e: org.springframework.web.servlet.resource.NoResourceFoundException): ResponseEntity<ApiResponse<Unit>> {
        val errorMessage = "요청하신 경로를 찾을 수 없습니다: ${e.resourcePath}"
        if (isLocalProfile) {
            log.warn("NoResourceFoundException occurred at {}: {}", findExceptionOrigin(e), errorMessage, e)
        } else {
            log.warn("NoResourceFoundException occurred: {}", errorMessage)
        }
        val response = ApiResponse.error<Unit>(ResponseCode.NOT_FOUND, errorMessage)
        return ResponseEntity.status(ResponseCode.NOT_FOUND.httpStatus).body(response)
    }

    /**
     * 그 외 예상치 못한 예외 처리
     */
    @ExceptionHandler(Exception::class)
    fun handleAllException(e: Exception): ResponseEntity<ApiResponse<Unit>> {
        if (isLocalProfile) {
            log.error("Unhandled Exception occurred at {}: {}", findExceptionOrigin(e), e.message, e)
        } else {
            log.error("Unhandled Exception occurred: ", e)
        }
        val response = ApiResponse.error<Unit>(ResponseCode.INTERNAL_ERROR)
        return ResponseEntity.status(ResponseCode.INTERNAL_ERROR.httpStatus).body(response)
    }

    private fun findExceptionOrigin(e: Throwable): String {
        val origin = e.stackTrace.firstOrNull {
            !it.className.startsWith("java.") &&
                !it.className.startsWith("jdk.") &&
                !it.className.startsWith("sun.reflect") &&
                !it.className.startsWith("org.springframework")
        } ?: return "unknown"

        return "${origin.className}.${origin.methodName}(${origin.fileName}:${origin.lineNumber})"
    }
}
