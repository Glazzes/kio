package com.kio.configuration.aws

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class S3Configuration(private val awsProperties: AwsProperties){

    @Bean
    fun amazonS3(): AmazonS3 {
        val (_, endpoint, region, accessKey, secretKey) = awsProperties

        val credentials = AWSStaticCredentialsProvider(BasicAWSCredentials(accessKey, secretKey))
        val endpointConfig = AwsClientBuilder.EndpointConfiguration(endpoint, region)

        return AmazonS3ClientBuilder.standard()
            .withCredentials(credentials)
            .withEndpointConfiguration(endpointConfig)
            .build()
    }

}