package com.kio.repositories

import org.springframework.data.jpa.repository.JpaRepository

interface FileRepository : JpaRepository<File, String>