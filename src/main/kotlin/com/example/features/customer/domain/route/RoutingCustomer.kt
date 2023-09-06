package com.example.routing



import com.example.features.customer.domain.modal.Users
import com.example.generateToken
import com.example.jwtConfig
import com.example.src.modal.*
import com.example.src.repository.DatabaseFactory
import com.example.utils.apiListResponse
import com.example.utils.apiResponse
import com.typesafe.config.ConfigFactory
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.config.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*
import kotlin.collections.ArrayList


fun Route.userRoute(
    db: DatabaseFactory
) {

        route("/Customers") {
            //Login
            post("/register") {
                val requestBody = call.receive<Users>()
                try {
                    if (requestBody.phone != null && requestBody.email != null && requestBody.name != null) {
                        val user = db.addUser(requestBody)
                        val generateToken = generateToken(
                            user, jwtConfig
                        )
                        call.respond(
                            HttpStatusCode.OK,
                            JwtResponse(generateToken, 200, "Registered Successfully")
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
                    apiResponse(
                        statusCode = 400,
                        message = "${e.message}",
                        status = false,
                        statusCodeApi = HttpStatusCode.BadRequest,
                    )

                }
            }
            post("/checkMobileNumberExist") {
                val requestBody = call.receive<Users>()
                try {
                    if (requestBody.phone != null) {
                        val isPhoneExist = db.checkNumberExist(requestBody.phone)
                        if (isPhoneExist) {
                            val generateToken = generateToken(
                                requestBody, jwtConfig
                            )
                            call.respond(
                                HttpStatusCode.OK,
                                CheckNumberExist(true, 200, true, generateToken)
                            )

                        } else {
                            call.respond(
                                HttpStatusCode.OK,
                                CheckNumberExist(false, 200, false, "no access")
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
                    apiResponse(
                        statusCode = 400,
                        message = "${e.message}",
                        status = false,
                        statusCodeApi = HttpStatusCode.BadRequest,
                    )

                }
            }

            authenticate("jwt") {
            post("/ExclusiveOffers") {
                try {
                    val value = call.receive<CityAailibilty>()
                    val product = db.getHomeAllProducts()
                    var exclusive = emptyList<HomeProducts>()
                    exclusive = product.filter { it.productExclusiveSelling }
                    apiListResponse(
                        HttpStatusCode.OK, statusCode = 200, ls = exclusive,
                        message = "fetched successfully",
                        status = true
                    )


                } catch (e: Exception) {
                    apiResponse(
                        statusCode = 400,
                        message = "${e.message}",
                        status = false,
                        statusCodeApi = HttpStatusCode.BadRequest,
                    )

                }
            }
            post("/ItemsCollections") {
                try {
                    val requestBody = call.receive<SearchByProductId>()
                    if (requestBody.ProductId != null) {
                        val listItems = db.getProductSubItems(requestBody.ProductId)
                        apiListResponse(
                            HttpStatusCode.OK, statusCode = 200, ls = listItems,
                            message = "fetched successfully",
                            status = true
                        )
                    }
                } catch (e: Exception) {
                    apiResponse(
                        statusCode = 400,
                        message = "${e.message}",
                        status = false,
                        statusCodeApi = HttpStatusCode.BadRequest,
                    )

                }
            }
            get("/getProductCategory") {
                try {
                    val product = db.getProductCategory()
                    apiListResponse(
                        HttpStatusCode.OK, statusCode = 200, ls = product,
                        message = "fetched successfully",
                        status = true
                    )

                } catch (e: Exception) {
                    apiResponse(
                        statusCode = 400,
                        message = "${e.message}",
                        status = false,
                        statusCodeApi = HttpStatusCode.BadRequest,
                    )

                }
            }
            post("/getUserDetails") {
                try {
                    val requestBody = call.receive<UserRequest>()
                    if (requestBody.phone != null) {
                        var user = db.getUserByPhone(requestBody.phone)

                      val order=  db.getAllOrder("Ordered")
                        val deliver=  db.getAllOrder("Delivered")
                        val cancel= db.getAllOrder("Cancelled")
                        user?.cancel=cancel.size.toString()
                        user?.deliver=deliver.size.toString()
                        user?.order=order.size.toString()

                        call.respond(status = HttpStatusCode.OK, UserProfileResponse(user, 200, "fetched successfully"))
                    } else {
                        call.respond(status = HttpStatusCode.OK, UserProfileResponse(null, 400, "mobile number incorrect"))

                    }
                } catch (e: Exception) {
                    apiResponse(
                        statusCode = 400,
                        message = "${e.message}",
                        status = false,
                        statusCodeApi = HttpStatusCode.BadRequest,
                    )

                }
            }


            get("/HomeAllProducts") {
                try {
                    val skip = call.parameters["skip"]
                    val limit = call.parameters["limit"]
                    val category = call.parameters["category"]
                    val product = db.getHomeAllProducts(Integer.parseInt(skip), Integer.parseInt(limit), category)
                    apiListResponse(
                        HttpStatusCode.OK, statusCode = 200, ls = product,
                        message = "fetched successfully",
                        status = true
                    )

                } catch (e: Exception) {
                    apiResponse(
                        statusCode = 400,
                        message = "${e.message}",
                        status = false,
                        statusCodeApi = HttpStatusCode.BadRequest,
                    )

                }
            }
            get("/SearchAllProducts") {
                try {
                    val value = call.parameters["query"]
                    val regex = Regex("${value}.*", RegexOption.IGNORE_CASE)
                    val product = db.getSearchAllProducts(regex)
                    apiListResponse(
                        HttpStatusCode.OK, statusCode = 200, ls = product,
                        message = "fetched successfully",
                        status = true
                    )

                } catch (e: Exception) {
                    apiResponse(
                        statusCode = 400,
                        message = "${e.message}",
                        status = false,
                        statusCodeApi = HttpStatusCode.BadRequest,
                    )

                }
            }
//        post("/SearchProduct"){
//            try {
//                val product = db.getHomeAllProducts()
//                call.respond(status = HttpStatusCode.OK, HomeProductResponse(product, 200, "fetched successfully"))
//
//            } catch (e: Exception) {
//                application.log.error("Failed to get data", e.message)
//                call.respond(status = HttpStatusCode.BadRequest, "${e.message}")
//            }
//        }


            post("/BestSelling") {
                try {
                    val product = db.getHomeAllProducts()
                    val best = product.filter { it.productBestSelling == true }
                    apiListResponse(
                        HttpStatusCode.OK, statusCode = 200, ls = best,
                        message = "fetched successfully",
                        status = true
                    )

                } catch (e: Exception) {
                    apiResponse(
                        statusCode = 400,
                        message = "${e.message}",
                        status = false,
                        statusCodeApi = HttpStatusCode.BadRequest,
                    )

                }
            }

            //calling product id
            post("/GetPendingProductById") {
                try {
                    val requestBody = call.receive<SearchByProductId>()
                    val product = db.GetPendingProductById(requestBody.ProductId!!)
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
                    apiResponse(
                        statusCode = 400,
                        message = "${e.message}",
                        status = false,
                        statusCodeApi = HttpStatusCode.BadRequest,
                    )

                }
            }
            post("/CreateOrderId") {
                val requestBodyItem = call.receive<orderitem>()
                try {
                    var allitemcalled: Boolean? = false
                    var lsInput: ArrayList<Orders> = ArrayList()
                    val cal = Calendar.getInstance()
                    requestBodyItem.createdDate = cal.time.toString()
                    requestBodyItem.orderId = "OD${System.currentTimeMillis()}"
                    print("request_body ${requestBodyItem.orderList}")
                    for (requestBody in requestBodyItem.orderList) {
                        if (requestBody.productprice != null && requestBody.productId != null && requestBody.productName != null && requestBody.quantity != null) {
                            lsInput.add(requestBody)
                            //  if(requestBodyItem.list.size-1==totalitem)
                            allitemcalled = true

                        }
                    }
                    if (allitemcalled == true) {
                        val order = db.orderdetails(requestBodyItem)
                        call.respond(
                            HttpStatusCode.OK,
                            BookedOrders(ProductResponse = order, statusCode = 200, message = "Order Placed successfully")
                        )

                    } else {
                        call.respond(
                            Message(
                                "Missing Item", false,
                                401
                            )
                        )
                    }
                } catch (e: Exception) {
                    apiResponse(
                        statusCode = 400,
                        message = "${e.message}",
                        status = false,
                        statusCodeApi = HttpStatusCode.BadRequest,
                    )

                }
            }
                post("/AllOrders") {
                try {
                    val status = call.receive<String>()
                    print("order_statatus_is ${status}")

                    val orders = db.getAllOrder(status)
                    print("order_statatus_is ${orders}")
                    apiListResponse(
                        HttpStatusCode.OK, statusCode = 200, ls = orders,
                        message = "fetched successfully",
                        status = true
                    )

                } catch (e: Exception) {
                    apiResponse(
                        statusCode = 400,
                        message = "${e.message}",
                        status = false,
                        statusCodeApi = HttpStatusCode.BadRequest,
                    )

                }
            }

            post("/GetBestProductById") {
                try {
                    val requestBody = call.receive<SearchByProductId>()
                    val product = db.getBestProductBasedId(requestBody.ProductId!!)
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
                    apiResponse(
                        statusCode = 400,
                        message = "${e.message}",
                        status = false,
                        statusCodeApi = HttpStatusCode.BadRequest,
                    )

                }
            }
            post("/GetRelatedSearch") {
                try {
                    val requestBody = call.receive<RelatedSerachByPriceAndCategory>()
                    val product = db.getRelatedSearch(requestBody.Price!!).take(4)
                    apiListResponse(
                        HttpStatusCode.OK, statusCode = 200, ls = product,
                        message = "fetched successfully",
                        status = true
                    )


                } catch (e: Exception) {
                    apiResponse(
                        statusCode = 400,
                        message = "${e.message}",
                        status = false,
                        statusCodeApi = HttpStatusCode.BadRequest,
                    )

                }
            }

            post("/GetExclusiveProductById") {
                try {
                    val requestBody = call.receive<SearchByProductId>()
                    val product = db.getExclusiveProductBasedId(requestBody.ProductId!!)
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
                    apiResponse(
                        statusCode = 400,
                        message = "${e.message}",
                        status = false,
                        statusCodeApi = HttpStatusCode.BadRequest,
                    )

                }
            }
                get("/HomeCategoryWiseProducts") {
                    try {
                        val allCategory = db.getProductCategory().filter {
                            it.category != "Best Selling"
                        }.filter { it.category != "exclusive" }.filter {
                            it.category != "best"
                        }
                        val product = db.getHomeAllProducts()
                        val cat1: List<HomeProducts> = product.filter { mainCategory ->
                            allCategory.lastOrNull()?.subCategoryList?.getOrNull(0)?.name == mainCategory.item_subcategory_name.toString()
                        }.take(4)
                        println("result iss ${cat1.size}")
                        val cat2: List<HomeProducts> = product.filter { mainCategory ->
                            allCategory.lastOrNull()?.subCategoryList?.getOrNull(1)?.name == mainCategory.item_subcategory_name.toString()
                        }.take(4)
                        println("result iss ${cat2.size}")
                        val cat3: List<HomeProducts> = product.filter { mainCategory ->
                            allCategory.lastOrNull()?.subCategoryList?.getOrNull(2)?.name == mainCategory.item_subcategory_name.toString()
                        }.take(4)
                        println("result iss ${cat3.size}")
                        val cat4: List<HomeProducts> = product.filter { mainCategory ->
                            allCategory.lastOrNull()?.subCategoryList?.getOrNull(3)?.name  == mainCategory.item_subcategory_name.toString()
                        }.take(4)
                        println("result iss ${cat4.size}")
                        val ls = mutableListOf<DashboardHomeProductsModal>()
                        allCategory.lastOrNull()?.subCategoryList?.size?.let {
                            ls.add(
                                DashboardHomeProductsModal(
                                    cat1,
                                    allCategory.lastOrNull()?.subCategoryList?.getOrNull(it - 1)?.name ?: "null",
                                    "${allCategory.lastOrNull()?.subCategoryList?.getOrNull(0)?.name}"
                                )
                            )
                            ls.add(
                                DashboardHomeProductsModal(
                                    cat2,
                                    allCategory.lastOrNull()?.subCategoryList?.getOrNull(it - 2)?.name ?: "null",
                                    "${allCategory.lastOrNull()?.subCategoryList?.getOrNull(1)?.name}",
                                )
                            )
                            ls.add(
                                DashboardHomeProductsModal(
                                    cat3,
                                    allCategory.lastOrNull()?.subCategoryList?.getOrNull(it - 3)?.name ?: "null",
                                    "${allCategory.lastOrNull()?.subCategoryList?.getOrNull(2)?.name}",
                                )
                            )
                            ls.add(
                                DashboardHomeProductsModal(
                                    cat4,
                                    allCategory.lastOrNull()?.subCategoryList?.getOrNull(it - 4)?.name ?: "null",
                                    "${allCategory.lastOrNull()?.subCategoryList?.getOrNull(3)?.name}",
                                )
                            )
                        }
                        apiListResponse(
                            HttpStatusCode.OK, statusCode = 200, ls = ls,
                            message = "fetched successfully",
                            status = true
                        )

                    } catch (e: Exception) {
                        apiResponse(
                            statusCode = 400,
                            message = "${e.message}",
                            status = false,
                            statusCodeApi = HttpStatusCode.BadRequest,
                        )

                    }
                }



            //   authenticate("auth-jwt") {
            get("/allusers") {
                try {
                    val user = db.getAllUsers()
                    call.respond(user)

                } catch (e: Exception) {
                    apiResponse(
                        statusCode = 400,
                        message = "${e.message}",
                        status = false,
                        statusCodeApi = HttpStatusCode.BadRequest,
                    )

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
                    apiResponse(
                        statusCode = 400,
                        message = "${e.message}",
                        status = false,
                        statusCodeApi = HttpStatusCode.BadRequest,
                    )

                }
            }
        }




}}

//}

