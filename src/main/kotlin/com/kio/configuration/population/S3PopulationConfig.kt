package com.kio.configuration.population

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import com.kio.configuration.properties.BucketConfigurationProperties
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Paths

@Component
class S3PopulationConfig(
    private val s3: AmazonS3,
    private val bucketProperties: BucketConfigurationProperties
){
    @Value("\${kio.default-pfp-key}") private lateinit var defaultProfilePictureKey: String

    fun saveDefaultProfilePicture() {
        val defaultPicture = ClassPathResource("s3/default.png")
        val contentType = Files.probeContentType(Paths.get(defaultPicture.path))

        val metadata = ObjectMetadata().apply {
            this.contentType = contentType
            this.contentLength = defaultPicture.contentLength()
        }

        s3.putObject(bucketProperties.profilePicturesBucket, defaultProfilePictureKey, defaultPicture.inputStream, metadata)
    }

}