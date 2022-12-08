package com.kio.repositories

import com.kio.entities.Folder
import com.kio.entities.enums.FolderType
import com.kio.entities.projections.NameProjection
import com.kio.entities.projections.SizeProjection
import com.kio.entities.projections.SubFoldersProjection
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface FolderRepository : MongoRepository<Folder, String> {
    fun findByFavoritesContains(authenticatedUserId: String, pageRequest: PageRequest): Page<Folder>
    fun findByMetadataOwnerIdAndFolderType(id: String, folderType: FolderType): Folder?
    fun findByIdIsIn(ids: Collection<String>): Collection<Folder>
    fun findByIdIsIn(ids: Collection<String>, pageRequest: PageRequest): Page<Folder>
    fun findBySharedWithContains(id: String): Collection<Folder>

    @Query(value = "{_id: {\$in: ?0}}")
    fun findSubFolderIdsByParentIds(ids: Collection<String>): Collection<SubFoldersProjection>
}