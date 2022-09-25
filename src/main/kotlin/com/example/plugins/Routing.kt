package com.example.plugins

import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*


import com.example.TokenManager
import com.example.src.data.*


import com.example.src.repository.DatabaseFactory
import com.typesafe.config.ConfigFactory
import io.ktor.server.auth.*
import io.ktor.server.config.*

import java.lang.Exception


fun Route.userRoute(
    db: DatabaseFactory
) {
    val tokenManager = TokenManager(HoconApplicationConfig(ConfigFactory.load()))
    route("/Customers") {
//        post("/addproduct"){
//
//            db.bestselling(BestSelling("Bell Pipper","50","1 kg"))
//            db.exclusiveoffers(ExclusiveOffers("Ginger","40","1 kg"))
//            db.homeproducts(HomeProducts("Beef Bone","30","1 kg",null))
//            call.respond("saved successfully")
//        }
        post("/ExclusiveOffers") {
            try {
                val product = db.getExcusiveAllProducts()
                call.respond(status = HttpStatusCode.OK, HomeProductResponse(product, 200, "fetched successfully"))

            } catch (e: Exception) {
                application.log.error("Failed to get data", e.message)
                call.respond("${e.message}")
            }
        }
        post("/HomeAllProducts") {
            try {
                val product = db.getHomeAllProducts()
                call.respond(status = HttpStatusCode.OK, HomeProductResponse(product, 200, "fetched successfully"))

            } catch (e: Exception) {
                application.log.error("Failed to get data", e.message)
                call.respond(status = HttpStatusCode.BadRequest, "${e.message}")
            }
        }
        post("/BestSelling") {
            try {
                val product = db.getBestAllProducts()
                call.respond(status = HttpStatusCode.OK, HomeProductResponse(product, 200, "fetched successfully"))

            } catch (e: Exception) {
                application.log.error("Failed to get data", e.message)
                call.respond("${e.message}")
            }
        }

        //calling product id
        post("/GetPendingProductById") {
            try {
                val requestBody = call.receive<SearchByProductId>()
                val product = db.GetPendingProductById(requestBody.ProductId)
                print("messageakis ${product} $requestBody")
                if (product?.productId != null)
                    call.respond(
                        status = HttpStatusCode.OK,
                        ProductResponseById(product, 200, "fetched successfully")
                    )
                else
                    call.respond(
                        status = HttpStatusCode.OK,
                        ProductResponseById(product, 400, "Items not available")
                    )

            } catch (e: Exception) {
                application.log.error("Failed to get data", e.message)
                call.respond(status = HttpStatusCode.BadRequest, "${e.message}")
            }
        }
        post("/GetBestProductById") {
            try {
                val requestBody = call.receive<SearchByProductId>()
                val product = db.getBestProductBasedId(requestBody.ProductId)
                print("messageakis ${product} $requestBody")
                if (product?.productId != null)
                    call.respond(
                        status = HttpStatusCode.OK,
                        ProductResponseById(product, 200, "fetched successfully")
                    )
                else
                    call.respond(
                        status = HttpStatusCode.OK,
                        ProductResponseById(product, 400, "Items not available")
                    )

            } catch (e: Exception) {
                application.log.error("Failed to get data", e.message)
                call.respond(status = HttpStatusCode.BadRequest, "${e.message}")
            }
        }
        post("/GetExclusiveProductById") {
            try {
                val requestBody = call.receive<SearchByProductId>()
                val product = db.getExclusiveProductBasedId(requestBody.ProductId)
                print("messageakis ${product} $requestBody")
                if (product?.productId != null)
                    call.respond(
                        status = HttpStatusCode.OK,
                        ProductResponseById(product, 200, "fetched successfully")
                    )
                else
                    call.respond(
                        status = HttpStatusCode.OK,
                        ProductResponseById(product, 400, "Items not available")
                    )

            } catch (e: Exception) {
                application.log.error("Failed to get data", e.message)
                call.respond(status = HttpStatusCode.BadRequest, "${e.message}")
            }
        }


//get profile


        //Login
        post("/register") {
            val requestBody = call.receive<Users>()
            print("request_body" + requestBody)


            try {
                if (requestBody.phone != null && requestBody.email != null && requestBody.name != null) {

                        val user = db.addUser(requestBody)
                        val token = tokenManager.generateJWTToken(user)
                        call.respond(
                            HttpStatusCode.OK,
                            JwtResponse(token, 200, "Registered Successfully")
                        )

                } else {
                    call.respond(
                        Message(
                            "Please fill all informations", false,
                            401
                        )
                    )


                }


            } catch (e: Exception) {
                application.log.error("Failed to register user", e.message)
                call.respond("${e.message}")
            }
        }
        post("/checkMobileNumberExist") {
            val requestBody = call.receive<Users>()

            try {
                if (requestBody.phone != null) {
                    val isPhoneExist = db.CheckNumberExist(requestBody.phone)
                    if (isPhoneExist) {
                        val token = tokenManager.generateJWTExistToken(requestBody.phone)
                        call.respond(
                            HttpStatusCode.OK,
                            CheckNumberExist(true, 200, true,token)
                        )

                    } else {
                        call.respond(
                            HttpStatusCode.OK,
                            CheckNumberExist(false, 400, true,"no access")
                        )
                    }
                } else {
                    call.respond(
                        Message(
                            "Phone number should not be empty", false,
                            200
                        )
                    )


                }


            } catch (e: Exception) {
                application.log.error("Failed to register user", e.message)
                call.respond(
                    Message(
                        "something went wrong", false,
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
    val status: Boolean,
    val statusCode: Int,

)
data class CheckNumberExist(val isMobileExist: Boolean,val statusCode: Int,val status: Boolean,val JwtToken:String)

data class JwtResponse(
    val token: String,
    val statusCode: Int,
    val message: String
)

data class HomeProductResponse(
    val list: List<Any>,
    val statusCode: Int,
    val message: String
)

data class ProductResponseById<T>(
    val ProductResponse: T?,
    val statusCode: Int,
    val message: String
)
