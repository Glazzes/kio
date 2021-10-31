package com.kio.events.file

import com.kio.entities.File
import javax.persistence.PostRemove

class FileEntityEventListener {

    @PostRemove
    fun onFileDelete(file: File){
        println("File has been deleted ${file.originalFilename}")
    }

}