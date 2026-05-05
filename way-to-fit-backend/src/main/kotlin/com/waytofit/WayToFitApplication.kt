package com.waytofit

import com.waytofit.global.config.CorsProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(CorsProperties::class)
class WayToFitApplication

fun main(args: Array<String>) {
    runApplication<WayToFitApplication>(*args)
}
