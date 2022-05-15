package com.kio.shared.utils

object FileUtils {

    // As operating systems do, if a file name is repeated the last one is given a number at the end of its name
    // file.png and file.png (1) are considered as equals, so each new file.png will get a new number
    fun getValidName(currentName: String, names: Collection<String>): String{
        var matches = 0
        val regex = Regex("$currentName\\s?(\\(\\d+\\))?")
        for(projection in names){
            if(regex.matches(projection)) matches++
        }

        return if(matches > 0) "$currentName ($matches)" else currentName
    }

}