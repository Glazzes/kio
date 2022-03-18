package com.kio.shared.utils

import com.kio.entities.mongo.projections.NameProjection

object FileUtils {

    // As operating systems do, if a file name is repeated this one is granted a number at the end
    // file.png and file.png (1) are considered as equals, so each new file.png will get a new number
    fun getValidName(currentName: String, names: Collection<NameProjection>): String{
        var matches = 0
        val regex = Regex("$currentName\\s?(\\(\\d+\\))?")
        for(projection in names){
            if(regex.matches(projection.getName())) matches++
        }

        return if(matches > 0) "$currentName $matches" else currentName
    }

}