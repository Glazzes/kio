package com.kio.entities

import org.hibernate.annotations.GenericGenerator
import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(name = "files")
class File(

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDHexGenerator")
    var id: String? = null,

    @Column(nullable = false)
    var filename: String? = null,

    @Column(name = "original_filename", updatable = false)
    var originalFilename: String? = null,

    @Column(updatable = false)
    var size: Long? = null,

    @ManyToOne
    @JoinColumn(name = "fk_folder_id", referencedColumnName = "id")
    var baseFolder: Folder? = null,
): Auditor()
