package com.kio

import com.kio.configuration.aws.AwsProperties
import com.kio.configuration.properties.OAuthConfigurationProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.scheduling.annotation.EnableAsync

@EnableAsync
@SpringBootApplication(
	exclude = [
		DataSourceAutoConfiguration::class,
		DataSourceTransactionManagerAutoConfiguration::class,
		HibernateJpaAutoConfiguration::class
	])
@EnableMongoAuditing
@EnableConfigurationProperties(value = [OAuthConfigurationProperties::class, AwsProperties::class])
class KioApplication

fun main(args: Array<String>) {
	runApplication<KioApplication>(*args)
}
