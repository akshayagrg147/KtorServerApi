package com.example.plugins

import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*


import com.example.TokenManager
import com.example.src.data.BestSelling
import com.example.src.data.ExclusiveOffers
import com.example.src.data.HomeProducts


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
    route("/Customers") {
        post("/addproduct"){

            db.bestselling(BestSelling("Bell Pipper","50","1 kg"))
            db.exclusiveoffers(ExclusiveOffers("Ginger","40","1 kg"))
            db.homeproducts(HomeProducts("Beef Bone","30","1 kg"))
            call.respond("saved successfully")
        }
        post("/ExclusiveOffers") {
            try {
                val product = db.getExcusiveAllProducts()
                call.respond(product)

            } catch (e: Exception) {
                application.log.error("Failed to get data", e.message)
                call.respond("${e.message}")
            }
        }
        post("/HomeAllProducts") {
            try {
                val product = db.getHomeAllProducts()
                call.respond(product)

            } catch (e: Exception) {
                application.log.error("Failed to get data", e.message)
                call.respond("${e.message}")
            }
        }
        post("/getProfile") {
            val requestBody = call.receive<Users>()


            try {
                if (requestBody.phone != null ) {
                    try {
                        val user = db.getUserByPhone(requestBody.phone)
                        call.respond(user!!)

                    } catch (e: Exception) {
                        call.respond(e.message.toString())
                        application.log.error("Failed to get data", e.message)
                    }
                    }else {
                    call.respond(
                        Message(
                            "Mobile Number should not be empty",false,
                            401
                        )
                    )


                }




            } catch (e: Exception) {
                application.log.error("Failed to register user", e.message)
                call.respond("${e.message}")
            }
        }

        post("/BestSelling") {
            try {
                val product = db.getBestAllProducts()
                call.respond(product)

            } catch (e: Exception) {
                application.log.error("Failed to get data", e.message)
                call.respond("${e.message}")
            }
        }
        post("/register") {
            val requestBody = call.receive<Users>()
            print("request_body"+requestBody)


            try {
                if (requestBody.phone != null && requestBody.email != null&& requestBody.name!=null) {
                    val isEmailExist = db.CheckNumberExist(requestBody.email)
                    if (isEmailExist) {
                        call.respond(Message(
                            "email already exist", false,HttpStatusCode.BadRequest.value
                            )
                        )
                    } else {
                        val user = db.addUser(requestBody)
                        val token = tokenManager.generateJWTToken(user)
                        call.respond(
                            HttpStatusCode.OK,
                            JwtResponse(token, 200,"Registered Successfully")
                        )
                    } }else {
                        call.respond(
                            Message(
                                "Please fill all informations",false,
                                401
                            )
                        )


                }




            } catch (e: Exception) {
                application.log.error("Failed to register user", e.message)
                call.respond("${e.message}")
            }
        }
        post("/login") {
            val requestBody = call.receive<Users>()

            try {
                if (requestBody.phone != null ) {
                    val isPhoneExist = db.CheckNumberExist(requestBody.phone)
                    if (isPhoneExist) {
                        call.respond(Message(
                            "phone number already exist",false,200)
                        )
                    } else {
                        call.respond(Message(
                            "phone number  not exist",true,200)
                        )
                    } } else {
                    call.respond(
                        Message(
                            "Phone number should not be empty",false,
                            200
                        )
                    )


                }




            } catch (e: Exception) {
                application.log.error("Failed to register user", e.message)
                call.respond(
                    Message(
                        "something went wrong",false,
                        401
                    )
                )
            }
        }
        authenticate("auth-jwt") {
            get("/allusers") {
                try {
                    val user = db.getAllUsers()
                    call.respond(user)

                } catch (e: Exception) {
                    application.log.error("Failed to get data", e.message)
                    call.respond("${e.message}")
                }
            }
//            get("/{id}") {
//                val id = call.parameters["id"]
//
//                try {
//                    val user = db.getUserById(id!!)
//                    call.respond(user)
//
//                } catch (e: Exception) {
//                    application.log.error("Failed to get data", e.message)
//                }
//            }
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
    val message: String,
    val status:Boolean,
    val statusCode: Int
)

data class JwtResponse(
    val token: String,
    val statusCode: Int,
    val message: String
)
