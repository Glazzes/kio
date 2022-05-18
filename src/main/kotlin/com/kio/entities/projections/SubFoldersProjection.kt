package com.kio.entities.projections

interface SubFoldersProjection {
    fun getSubFolders(): Collection<String>
}