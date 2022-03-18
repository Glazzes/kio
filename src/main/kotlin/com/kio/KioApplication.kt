package com.kio

import com.kio.configuration.aws.AwsProperties
import com.kio.configuration.properties.OAuthConfigurationProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.security.crypto.password.PasswordEncoder

@SpringBootApplication
@EnableMongoAuditing
@EnableConfigurationProperties(value = [OAuthConfigurationProperties::class, AwsProperties::class])
class KioApplication{
	@Autowired private lateinit var userRepository: UserRepository
	@Autowired private lateinit var passwordEncoder: PasswordEncoder

	@Bean
	fun init(): CommandLineRunner {
		return CommandLineRunner {
			val user = User(
				username = "glaze",
				password = passwordEncoder.encode("pass"),
				nickname = "glaze",
				email = "glaze@demo.com"
			)

			userRepository.save(user)
		}
	}

}

fun main(args: Array<String>) {
	runApplication<KioApplication>(*args)
}
