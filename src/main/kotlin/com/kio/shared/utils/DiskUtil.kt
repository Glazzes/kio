package com.kio.shared.utils

import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Paths

class DiskUtil {

    companion object{
        fun saveFileToDisk(file: FileInputStream, filename: String){
            try{
                val inputChannel = file.channel
                val outputChannel = FileOutputStream(filename).channel

                inputChannel.use { iChannel ->
                    outputChannel.use {
                        val byteBuffer = ByteBuffer.allocateDirect(Constants.BYTEBUFFER_CAPACITY)
                        while ((iChannel.read(byteBuffer)) != -1){
                            byteBuffer.flip()
                            it.write(byteBuffer)
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

        fun deleteFolderFromDisk(originalFolderName: String){
            Files.deleteIfExists(Paths.get(originalFolderName))
        }

    }

}