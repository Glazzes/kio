package com.kio.repositories

import com.kio.entities.File
import org.springframework.data.jpa.repository.JpaRepository

interface FileRepository : JpaRepository<File, String>