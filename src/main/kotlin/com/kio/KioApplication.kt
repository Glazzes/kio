package com.kio

import com.kio.configuration.properties.OAuthConfigurationProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(value = [OAuthConfigurationProperties::class])
class KioApplication

fun main(args: Array<String>) {
	runApplication<KioApplication>(*args)
}
