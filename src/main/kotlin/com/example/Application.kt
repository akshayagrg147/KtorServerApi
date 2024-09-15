package com.example

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.features.admin.domain.route.adminRoute
import com.example.features.customer.domain.route.userRoute
import com.example.src.repository.DatabaseFactory
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.typesafe.config.ConfigFactory
import io.ktor.client.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import java.io.FileInputStream

fun main() {
    val serviceAccount = object {}.javaClass.getResourceAsStream("/adminsdk.json")

    val options = FirebaseOptions.Builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
        .build()

    FirebaseApp.initializeApp(options)

    embeddedServer(Netty, port = 8083, host = "0.0.0.0") {
        val databaseFactory = DatabaseFactory()
        val config = HoconApplicationConfig(ConfigFactory.load())

        install(ContentNegotiation) {
            gson {
                setPrettyPrinting()
                disableHtmlEscaping()
            }
            // Register Kotlinx Serialization with JSON configuration for content negotiation
            json(
                Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                }
            )
        }

        install(Authentication) {
            jwt("jwt") {
                verifier(
                    JWT.require(Algorithm.HMAC256(jwtConfig.secret))
                        .withAudience(jwtConfig.audience)
                        .withIssuer(jwtConfig.issuer)
                        .build()
                )
                validate { credential ->
                    if (credential.payload.audience.contains(jwtConfig.audience)) {
                        JWTPrincipal(credential.payload)
                    } else {
                        null
                    }
                }
            }
        }

        routing {
            userRoute(databaseFactory)
            adminRoute(databaseFactory)
        }

        install(CORS) {
            // Allow requests from any host (use with caution in production)
            anyHost()
            // Alternatively, specify allowed hosts
            allowHost("localhost:49734")
            allowHost("0.0.0.0:8083")

            // Allow common HTTP methods
            allowMethod(HttpMethod.Options)
            allowMethod(HttpMethod.Put)
            allowMethod(HttpMethod.Patch)
            allowMethod(HttpMethod.Delete)
            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Post)

            // Allow headers
            allowHeader(HttpHeaders.XForwardedProto)
            allowHeader(HttpHeaders.ContentType)
            allowHeader(HttpHeaders.AccessControlAllowOrigin)

            // Support credentials and non-simple content types
            allowCredentials = true
            allowNonSimpleContentTypes = true

            // Set cache duration for preflight requests (20 days)
            maxAgeInSeconds = 1728000
        }

    }.start(wait = true)
}

// Email data class
data class EmailData(val to: String, val subject: String, val body: String)
