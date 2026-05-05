package com.waytofit.global.error

import com.waytofit.global.common.response.ResponseCode

open class BusinessException(
    val responseCode: ResponseCode,
    val overrideMessage: String? = null
) : RuntimeException(overrideMessage ?: responseCode.message)
