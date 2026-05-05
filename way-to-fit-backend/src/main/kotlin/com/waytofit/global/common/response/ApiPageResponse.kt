package com.waytofit.global.common.response

import org.springframework.data.domain.Page

data class ApiPageResponse<T>(
    val content: List<T>,
    val pageInfo: ApiPageInfo
) {
    companion object {
        fun <T> of(page: Page<T>): ApiPageResponse<T> {
            return ApiPageResponse(
                content = page.content,
                pageInfo = ApiPageInfo(
                    pageNumber = page.number + 1, // Spring의 0-based를 1-based로 변환
                    pageSize = page.size,
                    totalElements = page.totalElements,
                    totalPages = page.totalPages,
                    isFirst = page.isFirst,
                    isLast = page.isLast
                )
            )
        }
    }
}

data class ApiPageInfo(
    val pageNumber: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int,
    val isFirst: Boolean,
    val isLast: Boolean
)
