package com.kio.entities

import org.hibernate.annotations.GenericGenerator
import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(name = "files")
data class File(

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDHexGenerator")
    var id: String? = null,
    var filename: String? = null,
    var originalFilename: String? = null,
    var size: Long? = null,

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    var uploadedAt: LocalDate = LocalDate.now(),

    @Column(name = "last_modified", nullable = false)
    var lastModified: LocalDate = LocalDate.now()
)