package com.kio.repositories

import com.kio.entities.mongo.Folder
import com.kio.entities.mongo.projections.NameProjection
import com.kio.entities.mongo.projections.SizeProjection
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface FolderRepository : MongoRepository<Folder, String> {

    @Query(value = "{_id: {\$in: ?0}}")
    fun getSubFolderNames(ids: List<String?>): Collection<NameProjection>

    @Aggregation(value = ["{\$match: {_id: {\$in: ?0}}}", "{\$group: {_id: null, {totalSize: {\$sum: \"\$size\"}}}}"])
    fun getFolderSizeBySubFolderIds(ids: List<String?>): SizeProjection

}