package com.example

import com.example.plugins.*
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
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.HttpMethod
import io.ktor.server.plugins.cors.routing.*


fun main() {
    embeddedServer(Netty, port = 8084, host = "0.0.0.0") {
        val databaseFactory = DatabaseFactory()
        val config = HoconApplicationConfig(ConfigFactory.load())
        val tokenManager = TokenManager(config)
        routing{
            userRoute(databaseFactory)
            adminRoute(databaseFactory)
        }
        install(ContentNegotiation){
            gson {
                setPrettyPrinting()
                disableHtmlEscaping()
            }
        }
        install(CORS) {
            allowMethod(HttpMethod.Options)
            allowMethod(HttpMethod.Put)
            allowMethod(HttpMethod.Delete)
            allowMethod(HttpMethod.Patch)
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Get)
            allowNonSimpleContentTypes = true
            anyHost()

        }

        install(Authentication) {

            jwt("auth-jwt") {

                verifier(tokenManager.verifyJWTToken())
                challenge { defaultScheme, realm ->
                    call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
                }
                realm = config.property("realm").getString()
                validate { jwtCredential ->

                    if(jwtCredential.payload.getClaim("username").asString().isNotEmpty()) {
                        JWTPrincipal(jwtCredential.payload)
                    } else {
                       null
                    }
                }
            }

        }

    }.start(wait = true)



}
