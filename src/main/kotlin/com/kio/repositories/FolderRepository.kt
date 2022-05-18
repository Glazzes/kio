package com.kio.repositories

import com.kio.entities.Folder
import com.kio.entities.enums.FolderType
import com.kio.entities.projections.NameProjection
import com.kio.entities.projections.SizeProjection
import com.kio.entities.projections.SubFoldersProjection
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface FolderRepository : MongoRepository<Folder, String> {

    fun findByMetadataOwnerIdAndFolderType(id: String, folderType: FolderType): Folder?
    fun findByIdIsIn(ids: Collection<String>): Collection<Folder>

    @Query(value = "{_id: {\$in: ?0}}")
    fun findSubFolderIdsByParentIds(ids: Collection<String>): Collection<SubFoldersProjection>

    @Query(value = "{parentFolder: ?0}")
    fun findFolderNamesByParentId(parentFolder: String): Collection<NameProjection>
}