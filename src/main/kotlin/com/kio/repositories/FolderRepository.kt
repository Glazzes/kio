package com.kio.repositories

import com.kio.entities.Folder
import com.kio.entities.enums.FolderType
import com.kio.entities.projections.NameProjection
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface FolderRepository : MongoRepository<Folder, String> {

    fun findByMetadataOwnerAndFolderType(id: String, folderType: FolderType): Folder?
    fun findByIdIn(ids: Collection<String>): Collection<Folder>

    @Query(value = "{_id: {\$in: ?0}}")
    fun getSubFolderNames(ids: List<String?>): Collection<NameProjection>

    /*
    @Aggregation(value = ["{\$match: {_id: {\$in: ?0}}}", "{\$group: {_id: null, {totalSize: {\$sum: \"\$size\"}}}}"])
    fun findFolderSizeBySubFolderIds(ids: Collection<String?>): SizeProjection
     */
}