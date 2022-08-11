package com.example.src.routes



import com.example.TokenManager
import com.example.src.data.Users
import com.example.src.repository.DatabaseFactory
import com.typesafe.config.ConfigFactory
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.application

import java.lang.Exception
import kotlin.text.get


fun Route.userRoute(
    db: DatabaseFactory
) {
    val tokenManager = TokenManager(HoconApplicationConfig(ConfigFactory.load()))
    route("/user") {
        post("/") {
            val requestBody = call.receive<Users>()

            try {


                if(requestBody.password != null && requestBody.email!=null) {

                    val user = db.addUser(requestBody)
                    val token = tokenManager.generateJWTToken(user)
                    call.respond(HttpStatusCode.OK,
                        JwtResponse( token,200 ))
                    call.respond(user)
                }
                else{
                    call.respond(Message(
                        "check your password",
                        HttpStatusCode.BadRequest.value
                    ))
                }



            } catch (e: Exception) {
                application.log.error("Failed to register user", e.message)
                call.respond("${e.message}")
            }
        }
        authenticate("auth-jwt"){
            get ("/allusers"){
                try {

                    val user = db.getAllUsers()
                    call.respond(user)

                } catch (e: Exception) {
                    application.log.error("Failed to get data", e.message)
                    call.respond("${e.message}")
                }
            }
            get("/{id}") {
                val id = call.parameters["id"]

                try {
                    val user = db.getUserById(id!!)
                    call.respond(user)

                } catch (e: Exception) {
                    application.log.error("Failed to get data", e.message)
                }
            }
            delete("/{id}") {
                val id = call.parameters["id"]

                try {
                    val delete = db.deleteUserById(id!!)
                    if (delete) {
                        call.respond("user delete")
                    } else {
                        call.respond("user not found")
                    }

                } catch (e: Exception) {
                    application.log.error("Failed to get data", e.message)
                }
            }
        }




    }

}

data class Message(
    val message:String,
    val statusCode:Int
)
data class JwtResponse(
    val message:String,
    val statusCode:Int
)