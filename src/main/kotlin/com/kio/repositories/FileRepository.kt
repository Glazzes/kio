package com.kio.repositories

import com.kio.entities.File
import com.kio.entities.projections.NameProjection
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface FileRepository : MongoRepository<File, String> {

    fun findByIdIsIn(ids: Collection<String>): Collection<File>

    @Query(value = "{parentFolder: ?0}")
    fun findFilesNamesByParentId(id: String): Collection<NameProjection>

    @Query(value = "{_id: {\$in: ?0}}")
    fun getFolderFilesNames(ids: Set<String?>): Collection<NameProjection>

}