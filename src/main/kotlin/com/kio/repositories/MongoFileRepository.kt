package com.kio.repositories

import com.kio.entities.mongo.File
import com.kio.entities.mongo.projections.NameProjection
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface MongoFileRepository : MongoRepository<File, String> {

    @Query(value = "{_id: {\$in: :ids}}")
    fun getSubFileNames(ids: Set<String?>): Collection<NameProjection>

}