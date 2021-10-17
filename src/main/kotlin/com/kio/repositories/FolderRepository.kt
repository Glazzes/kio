package com.kio.repositories

import com.kio.entities.Folder
import org.springframework.data.jpa.repository.JpaRepository

interface FolderRepository : JpaRepository<Folder, String> {
    fun findAllByCreatedById(id: String): MutableList<Folder>
}