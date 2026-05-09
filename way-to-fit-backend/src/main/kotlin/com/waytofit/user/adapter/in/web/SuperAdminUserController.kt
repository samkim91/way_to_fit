package com.waytofit.user.adapter.`in`.web

import com.waytofit.global.common.response.ApiResponse
import com.waytofit.user.application.port.`in`.UserCommandUseCase
import com.waytofit.user.domain.User
import com.waytofit.user.domain.enums.UserRole
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.UUID

@Tag(name = "Super Admin - User", description = "슈퍼 어드민 사용자 관리 API")
@RestController
@RequestMapping("/api/v1/super-admin/users")
class SuperAdminUserController(
    private val userCommandUseCase: UserCommandUseCase,
) {

    @Operation(summary = "사용자 역할 변경")
    @PatchMapping("/{userId}/role")
    fun updateUserRole(
        @PathVariable userId: UUID,
        @RequestBody request: UpdateUserRoleRequest,
    ): ApiResponse<UserRoleResponse> {
        val user = userCommandUseCase.updateRole(userId, request.role)
        return ApiResponse.success(UserRoleResponse.from(user))
    }
}

data class UpdateUserRoleRequest(
    val role: UserRole,
)

data class UserRoleResponse(
    val id: UUID,
    val email: String?,
    val name: String,
    val role: UserRole,
    val updatedAt: Instant?,
) {
    companion object {
        fun from(user: User) = UserRoleResponse(
            id = user.id!!,
            email = user.email,
            name = user.name,
            role = user.role,
            updatedAt = user.audit.lastModifiedAt,
        )
    }
}
