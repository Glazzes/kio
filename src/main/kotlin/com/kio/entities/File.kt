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

    var filename: String,

    @Column(name = "original_filename", updatable = false)
    var originalFilename: String,

    @Column(updatable = false)
    var size: Long? = null,

    @ManyToOne
    @JoinColumn(name = "fk_folder_id", referencedColumnName = "id")
    var baseFolder: Folder? = null,
): Auditor()
