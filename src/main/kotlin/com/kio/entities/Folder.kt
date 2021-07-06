package com.kio.entities

import org.hibernate.annotations.GenericGenerator
import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(name = "folders")
class Folder(

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
    @JoinTable(
        name = "folder_subfolders",
        joinColumns = [JoinColumn(name = "base_folder")],
        inverseJoinColumns = [JoinColumn(name = "sub_folder")]
    )
    var subFolders: MutableList<Folder> = mutableListOf(),

    @OneToMany(mappedBy = "baseFolder")
    var files: MutableList<File> = mutableListOf()
)