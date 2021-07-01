package com.kio.entities

import org.hibernate.annotations.GenericGenerator
import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(name = "folders")
data class Folder(

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDHexGenerator")
    var id: String? = null,
    var folderName: String? = null,
    var createdAt: LocalDate? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_user_id", referencedColumnName = "id")
    var owner: User? = null,

    @OneToMany
    var subFolders: MutableList<Folder> = mutableListOf(),

    @OneToMany
    var files: MutableList<File> = mutableListOf()
)