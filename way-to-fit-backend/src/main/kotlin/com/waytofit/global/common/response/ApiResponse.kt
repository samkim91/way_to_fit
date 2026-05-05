package com.waytofit.global.common.response

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val code: String,
    val message: String,
    val data: T? = null
) {
    companion object {
        fun <T> success(data: T? = null): ApiResponse<T> {
            return ApiResponse(
                code = ResponseCode.SUCCESS.code,
                message = ResponseCode.SUCCESS.message,
                data = data
            )
        }

        fun <T> success(responseCode: ResponseCode, data: T? = null): ApiResponse<T> {
            return ApiResponse(
                code = responseCode.code,
                message = responseCode.message,
                data = data
            )
        }

        fun <T> error(responseCode: ResponseCode, message: String? = null): ApiResponse<T> {
            return ApiResponse(
                code = responseCode.code,
                message = message ?: responseCode.message
            )
        }
    }
}
