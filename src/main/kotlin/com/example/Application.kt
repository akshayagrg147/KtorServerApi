package com.example

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.features.admin.domain.route.adminRoute
import com.example.features.customer.domain.route.userRoute
import com.example.src.repository.DatabaseFactory

import com.typesafe.config.ConfigFactory
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.http.HttpMethod
import io.ktor.server.plugins.cors.routing.*


fun main() {
    embeddedServer(Netty, port = 8081, host = "0.0.0.0") {
        val databaseFactory = DatabaseFactory()
        val config = HoconApplicationConfig(ConfigFactory.load())
        install(ContentNegotiation){
            gson {
                setPrettyPrinting()
                disableHtmlEscaping()
            }
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

        routing{
            userRoute(databaseFactory)
            adminRoute(databaseFactory)
        }



        install(CORS) {
            // Set to true if you want to allow the same origin. It's false by default.
            anyHost()
            allowHost("localhost:53750")
            allowHost("0.0.0.0:8084")
            // Alternatively, specify allowed hosts:
            // host("my-host:8080")
            // host("my-host2:8080", schemes = listOf("http", "https"))

            // Specify which headers can be used in a request
            allowHeader(HttpHeaders.XForwardedProto)
            allowHeader(HttpHeaders.ContentType)
            allowHeader(HttpHeaders.AccessControlAllowOrigin)

            // If you don't want to support a header in your request, you can exclude it
            // exposeHeader("")

            // Specify which HTTP methods are allowed. By default, it allows only GET, POST, and HEAD.
            allowMethod(HttpMethod.Options)
            allowMethod(HttpMethod.Put)
            allowMethod(HttpMethod.Patch)
            allowMethod(HttpMethod.Delete)
            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Post)

            // You can also apply settings like:
            allowCredentials = true
            allowSameOrigin=true
            allowNonSimpleContentTypes = true
            allowNonSimpleContentTypes = true
            maxAgeInSeconds = 1728000 // Specifies how long the results of a preflight request can be cached. 20 days here.
        }


    }.start(wait = true)



}
data class EmailData(val to: String, val subject: String, val body: String)

