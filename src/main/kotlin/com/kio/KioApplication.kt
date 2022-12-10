package com.kio

import com.kio.configuration.aws.AwsProperties
import com.kio.configuration.population.S3PopulationConfig
import com.kio.configuration.properties.BucketConfigurationProperties
import com.kio.configuration.properties.OAuth2ConfigurationProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.scheduling.annotation.EnableAsync

@EnableAsync
@EnableMongoAuditing
@EnableMongoRepositories
@EnableConfigurationProperties(
	value = [
		OAuth2ConfigurationProperties::class,
		AwsProperties::class,
		BucketConfigurationProperties::class,
	])
@SpringBootApplication(
	exclude = [
		HibernateJpaAutoConfiguration::class,
		DataSourceAutoConfiguration::class,
		DataSourceTransactionManagerAutoConfiguration::class
	])
class KioApplication

fun main(args: Array<String>) {
	runApplication<KioApplication>(*args)
}
