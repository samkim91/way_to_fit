package com.waytofit.global.persistence

import com.waytofit.global.util.SecurityUtils
import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.security.core.context.SecurityContextHolder
import java.time.Instant

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity {
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null
        protected set

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    var createdBy: String? = null
        protected set

    @LastModifiedDate
    @Column(name = "last_modified_at", nullable = false)
    var lastModifiedAt: Instant? = null
        protected set

    @LastModifiedBy
    @Column(name = "last_modified_by")
    var lastModifiedBy: String? = null
        protected set

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null
        protected set

    @Column(name = "deleted_by")
    var deletedBy: String? = null
        protected set

    fun softDelete() {
        this.deletedAt = Instant.now()
        this.deletedBy = SecurityUtils.getCurrentAuditor()
    }
}
