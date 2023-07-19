package com.example.routing


import com.example.TokenManager
import com.example.src.modal.*
import com.example.src.repository.DatabaseFactory
import com.example.utils.apiListResponse
import com.example.utils.apiResponse
import com.typesafe.config.ConfigFactory
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Route.adminRoute(
    db: DatabaseFactory
) {
    val tokenManager = TokenManager(HoconApplicationConfig(ConfigFactory.load()))
    route("/Admin") {
        get("/allProducts") {
            try {
                val product = db.getHomeAllProducts()
                apiListResponse(HttpStatusCode.OK, statusCode = 200, ls =product ,
                message = "fetched successfully",
                status = true)

            } catch (e: Exception) {
                apiResponse(statusCode = 400 , message ="${e.message}",status = false, statusCodeApi = HttpStatusCode.BadRequest, )

            }
        }

        post("/Login") {
            try {
                val value = call.receive<RequestLoginBody>()
                if (value.email == "akshaygarg147@gmail.com" && value.password == "123456789") {
                    apiResponse(statusCode = 200 , message ="Successfully Logged In",status = true, statusCodeApi = HttpStatusCode.OK, )

                } else {
                    apiResponse(statusCode = 200 , message ="Incorrect Information's",status = false , statusCodeApi = HttpStatusCode.OK)
                }
            } catch (e: Exception) {
                apiResponse(statusCode = 400 , message ="${e.message}",status = false, statusCodeApi = HttpStatusCode.BadRequest, )

            }
        }
        post("/AddProduct") {
            try {
                var requestBody = call.receive<HomeProducts>()
                //BestSelling
                val specialCategories = listOf("Best Selling", "Exclusive", "Essential Products")
                if (specialCategories.any { requestBody.item_category_name?.contains(it, ignoreCase = true) == true }) {
                    println("request_catch_from_AddProduct ${db.getLastProductId()}")
                    var value = ((db.getLastProductId()?.toInt() ?: 1)).toString()
                    if (value != "1")
                        value = (value.toInt() + 1).toString()
                    val obj = bestSelling(productId = "")
                    obj.productName = requestBody.productName
                    obj.selling_price = requestBody.selling_price.toString()
                    obj.quantity = requestBody.quantity
                    obj.ProductImage1 = requestBody.ProductImage1
                    obj.ProductImage2 = requestBody.ProductImage2
                    obj.ProductImage3 = requestBody.ProductImage3
                    obj.productId = value
                    obj.ProductDescription = requestBody.ProductDescription
                    obj.orignal_price = requestBody.orignal_price
                    obj.dashboardDisplay = requestBody.dashboardDisplay
                    obj.rating = requestBody.rating
                    obj.item_category_name = requestBody.item_category_name


                    db.addBestSellingAdminDashboard(obj)
                    db.addProductAdminDashboard(requestBody);
                    call.respond(
                        status = HttpStatusCode.OK,
                        ApiResponse(status = true, statusCode = 200, message = "Added product Successfully")
                    )
                }
                //itemcollection
                else {
                    println("request_catch_from_AddProduct else ${db.getItemCount()} ")
                    if (db.getItemCount() <= 0) {
                        requestBody.productId = 1.toString()
                    } else {
                        var value = ((db.getLastProductId()?.toInt() ?: 1)).toString()
                        value = (value.toInt() + 1).toString()
                        requestBody.productId = value
                    }
                    println("request_catch_from_AddProduct else ${requestBody}  ")
                    db.addProductAdminDashboard(requestBody)
                    apiResponse(statusCode = 200 , message ="Added product Successfully",status = true, statusCodeApi = HttpStatusCode.OK, )

                }
                requestBody.productId


            } catch (e: Exception) {
                apiResponse(statusCode = 400 , message ="${e.message}",status = false, statusCodeApi = HttpStatusCode.BadRequest, )

            }
        }
        get("/getProductCategory") {
            try {
                val product = db.getProductCategory()
                apiListResponse(HttpStatusCode.OK, statusCode = 200, ls =product ,
                    message = "fetched successfully",
                    status = true)

            } catch (e: Exception) {
                apiResponse(statusCode = 400 , message ="${e.message}",status = false, statusCodeApi = HttpStatusCode.BadRequest, )


            }
        }

        post("/AddItemCategory") {
            try {
                var requestBody = call.receive<ProductCategory>()
                val response = db.addCategory(requestBody)
                call.respond(
                    status = HttpStatusCode.OK,
                    Message(statusCode = 200, message = "Added successfully", status = true)
                )
            } catch (e: Exception) {
                apiResponse(statusCode = 400 , message ="${e.message}",status = false, statusCodeApi = HttpStatusCode.BadRequest, )

            }

        }
        get("/RecentOrderCount") {
            try {
                val orders = db.getAllOrder()
                val ordersFilter = orders.take(10)
                val product = db.getHomeAllProducts()
                val users = db.getAlUsers()
                call.respond(
                    status = HttpStatusCode.OK,
                    CountResponse(
                        image = "https://ik.imagekit.io/00itvcwwk/default-image.jpg?updatedAt=1687762479357",
                        name = "Akshay Garg",
                        ordersFilter,
                        listOf(
                            ItemCount(
                                "ProductItems",
                                "https://ik.imagekit.io/00itvcwwk/Dashboard_Admin/MicrosoftTeams-image__20_.png?updatedAt=1689506162992",
                                product.size
                            ),
                            ItemCount(
                                "Users",
                                "https://ik.imagekit.io/00itvcwwk/Dashboard_Admin/MicrosoftTeams-image__22_.png?updatedAt=1689506163196",
                                users.size
                            ),
                            ItemCount(
                                "Orders",
                                "https://ik.imagekit.io/00itvcwwk/Dashboard_Admin/MicrosoftTeams-image__19_.png?updatedAt=1689506163736",
                                orders.size
                            ),
                            ItemCount(
                                "Category",
                                "https://ik.imagekit.io/00itvcwwk/Dashboard_Admin/MicrosoftTeams-image__21_.png?updatedAt=1689506163236",
                                orders.size
                            )
                        ),
                        200,
                        "fetched successfully"
                    )
                )

            } catch (e: Exception) {
                apiResponse(statusCode = 400 , message ="${e.message}",status = false, statusCodeApi = HttpStatusCode.BadRequest, )

            }
        }
        get("/AllOrdersByPages") {
            try {
                val skip = call.parameters["skip"]
                val limit = call.parameters["limit"]
                val orders = db.getAllOrderPagination(Integer.parseInt(skip), Integer.parseInt(limit))
                apiListResponse(HttpStatusCode.OK, statusCode = 200, ls =orders ,
                    message = "fetched successfully",
                    status = true)

            } catch (e: Exception) {
                apiResponse(statusCode = 400 , message ="${e.message}",status = false, statusCodeApi = HttpStatusCode.BadRequest, )

            }
        }
        delete("/deleteProduct/{product_id}") {
            val productId = call.parameters["product_id"]
        }

        post("/updateProduct") {
            var requestBody = call.receive<HomeProducts>()
            if (db.updateProduct(requestBody) > 0) {
                call.respond(
                    status = HttpStatusCode.OK,
                    ApiResponse(status = true, statusCode = 200, message = "Updated quantity Successfully")
                )
            } else {
                call.respond(
                    status = HttpStatusCode.OK,
                    ApiResponse(status = false, statusCode = 200, message = "Incorrect product Id")
                )
            }

        }
        delete("deleteProduct/{productId}") {
            val id = call.parameters["productId"]

            try {
                val delete = db.deleteProductById(id!!)
                if (delete) {
                    call.respond(
                        status = HttpStatusCode.OK,
                        ApiResponse(status = true, statusCode = 200, message = "Deleted product Successfully")
                    )
                    call.respond("user delete")
                } else {
                    call.respond(
                        status = HttpStatusCode.OK,
                        ApiResponse(status = true, statusCode = 200, message = "Product not Deleted")
                    )
                }

            } catch (e: Exception) {
                apiResponse(statusCode = 400 , message ="${e.message}",status = false, statusCodeApi = HttpStatusCode.BadRequest, )

            }
        }
        get("/AllUsers") {
            try {
                val users = db.getAlUsers()
                apiListResponse(HttpStatusCode.OK, statusCode = 200, ls =users ,
                    message = "fetched successfully",
                    status = true)
            } catch (e: Exception) {
                apiResponse(statusCode = 400 , message ="${e.message}",status = false, statusCodeApi = HttpStatusCode.BadRequest, )

            }
        }
        post("/getAddedPrd") { }


    }
}

fun addAccessControlHeaders(call: ApplicationCall) {
    call.response.headers.append(HttpHeaders.AccessControlAllowOrigin, "*")
    call.response.headers.append(HttpHeaders.AccessControlAllowMethods, HttpMethod.Get.toString())
    call.response.headers.append(HttpHeaders.AccessControlAllowMethods, HttpMethod.Head.toString())
}



