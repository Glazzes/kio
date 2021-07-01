package com.kio.repositories

import com.kio.entities.Folder
import org.springframework.data.jpa.repository.JpaRepository

interface FolderRepository : JpaRepository<Folder, String> {
    fun findAllByOwnerId(id: String?): MutableList<Folder>
}