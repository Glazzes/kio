package com.kio.entities

import org.hibernate.annotations.GenericGenerator
import javax.persistence.*

@Entity
@Table(name = "folders")
class Folder(

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDHexGenerator")
    var id: String? = null,

    var originalFolderName: String,
    var folderName: String,
    var spaceUsed: Long = 0,

    @OneToMany
    @JoinTable(
        name = "folder_subfolders",
        joinColumns = [JoinColumn(name = "base_folder_id")],
        inverseJoinColumns = [JoinColumn(name = "sub_folder_id")]
    )
    var subFolders: MutableList<Folder> = mutableListOf(),

    @OneToMany(mappedBy = "baseFolder")
    var files: MutableList<File> = mutableListOf()
): Auditor()