package com.kio.shared.utils

import org.springframework.web.multipart.MultipartFile
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Paths

class DiskUtil {

    companion object{
        fun saveFileToDisk(file: MultipartFile){
            try{
                (file.inputStream as FileInputStream).use { fic ->
                    val inputChannel = fic.channel
                    val output = FileOutputStream("${Constants.DEFAULT_DIRECTORY}${file.originalFilename}")

                    output.use { foc ->
                        val outputChannel = foc.channel
                        val byteBuffer = ByteBuffer.allocateDirect(Constants.BYTEBUFFER_CAPACITY)

                        while ((inputChannel.read(byteBuffer)) != -1){
                            byteBuffer.flip()
                            outputChannel.write(byteBuffer)
                            byteBuffer.clear()
                        }
                    }
                }
            }catch (e: IOException){
                e.printStackTrace()
            }
        }

        fun deleteFileFromDisk(filename: String){
            val filePath = Paths.get("${Constants.DEFAULT_DIRECTORY}$filename")
            if(Files.exists(filePath)){
                Files.delete(filePath)
            }else{
                throw FileNotFoundException("Can not delete file because it does not exists.")
            }
        }

    }

}