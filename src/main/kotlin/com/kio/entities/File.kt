package com.kio.entities

import com.kio.events.file.FileEntityEventListener
import org.hibernate.annotations.GenericGenerator
import javax.persistence.*

@Entity
@Table(name = "files")
@EntityListeners(FileEntityEventListener::class)
class File(

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDHexGenerator")
    var id: String? = null,

    var filename: String,

    @Column(name = "original_filename", updatable = false)
    var originalFilename: String,

    @Column(updatable = false)
    var size: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_folder_id", referencedColumnName = "id")
    var parentFolder: Folder? = null,

): Auditor()
