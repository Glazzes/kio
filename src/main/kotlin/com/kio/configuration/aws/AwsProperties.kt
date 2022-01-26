package com.kio.configuration.aws

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "aws.s3")
data class AwsProperties(
    val bucket: String,
    val endpoint: String,
    val region: String,
    val accessKey: String,
    val secretKey: String
)