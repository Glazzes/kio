package com.kio.shared.utils

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.kio.valueobjects.AudioSamples
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import org.springframework.beans.factory.annotation.Value
import org.springframework.util.FileCopyUtils
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID
import javax.imageio.ImageIO

object MetadataUtil {

    private val tmpDir = System.getProperty("java.io.tmpdir")

    fun getAudioSamples(file: MultipartFile): Array<Int> {
        val transferPath = "${tmpDir}/${UUID.randomUUID()}-${file.originalFilename}"
        file.transferTo(Paths.get(transferPath))

        val jsonPath = "${tmpDir}/${UUID.randomUUID()}.json"
        val pb = ProcessBuilder(
            "/usr/bin/audiowaveform",
            "-i", transferPath,
            "-o", jsonPath,
            "-b", "8",
            "--pixels-per-second", "2")

        val process = pb.start()
        process.waitFor()
        val audioData = jacksonObjectMapper().readValue(File(jsonPath), AudioSamples::class.java)
        cleanUp(transferPath, jsonPath)

        return audioData.data
    }

    fun getPdfMetadata(file: MultipartFile): ByteArray {
        val thumbnailPath = "${tmpDir}/${UUID.randomUUID()}.png"

        val pdf = PDDocument.load(file.inputStream)
        val renderer = PDFRenderer(pdf).renderImage(0)
        ImageIO.write(renderer, "png", File(thumbnailPath))
        val bytes = Files.readAllBytes(Paths.get(thumbnailPath))
        cleanUp(thumbnailPath)

        return bytes
    }

    private fun cleanUp(vararg paths: String) {
        try {
            for(path in paths) {
                Files.deleteIfExists(Paths.get(path))
            }
        }catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

}