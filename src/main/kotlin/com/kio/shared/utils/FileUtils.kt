package com.kio.shared.utils

import com.amazonaws.services.s3.model.AmazonS3Exception
import com.amazonaws.services.s3.model.S3ObjectInputStream
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import java.io.IOException
import java.io.InputStream

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

    fun getStreamingResponseBodyFromObjectContent(content: S3ObjectInputStream) = StreamingResponseBody {
        it.use { out ->
            try{
                val bytes = ByteArray(8)
                while (content.read(bytes) != -1) {
                    out.write(bytes)
                }
            }catch (e: AmazonS3Exception) {
                content.abort()
            }finally {
                content.abort()
                content.close()
            }
        }
    }

}