package com.kio.repositories

import com.kio.entities.File
import com.kio.entities.projections.NameProjection
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface FileRepository : MongoRepository<File, String> {

    fun findByMetadataOwnerIdAndIsFavorite(ownerId: String, isFavorite: Boolean, pageRequest: PageRequest): Page<File>
    fun findByIdIsIn(ids: Collection<String>): Collection<File>
    fun findByIdIsIn(ids: Collection<String>, pageRequest: PageRequest): Page<File>

    @Query(value = "{parentFolder: ?0}")
    fun findFilesNamesByParentId(id: String): Collection<NameProjection>

    @Query(value = "{_id: {\$in: ?0}}")
    fun getFolderFilesNames(ids: Set<String?>): Collection<NameProjection>

}