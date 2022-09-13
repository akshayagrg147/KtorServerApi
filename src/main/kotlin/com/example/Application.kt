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

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        val databaseFactory = DatabaseFactory()
        val config = HoconApplicationConfig(ConfigFactory.load())
        val tokenManager = TokenManager(config)
        routing{
            userRoute(databaseFactory)
        }
        install(ContentNegotiation){
            gson {
                setPrettyPrinting()
                disableHtmlEscaping()
            }
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
