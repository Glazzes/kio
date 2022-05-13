package com.kio.configuration.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "aws.s3")
data class BucketConfigurationProperties(
    val filesBucket: String,
    val profilePicturesBucket: String
)