package com.kio.entities

import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDate
import javax.persistence.Column
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.MappedSuperclass

@MappedSuperclass
abstract class Auditor(

    @CreatedBy
    @ManyToOne
    @JoinColumn(name = "fk_user_id", nullable = false, updatable = false)
    var createdBy: User? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDate? = null,

    @LastModifiedDate
    @Column(name = "last_modified", nullable = false)
    var lastModified: LocalDate? = null
)