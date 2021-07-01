package com.kio

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KioApplication

fun main(args: Array<String>) {
	runApplication<KioApplication>(*args)
}
