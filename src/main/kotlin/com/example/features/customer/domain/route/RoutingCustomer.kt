package com.example.features.customer.domain.route


import com.example.features.customer.domain.modal.Users
import com.example.generateToken
import com.example.jwtConfig
import com.example.src.modal.*
import com.example.src.repository.DatabaseFactory
import com.example.utils.apiListResponse
import com.example.utils.apiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.text.SimpleDateFormat
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
                        HttpStatusCode.OK, JwtResponse(generateToken, 200, "Registered Successfully")
                    )
                } else {
                    call.respond(
                        Message(
                            "Please fill all informations", false, 401
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
                            HttpStatusCode.OK, CheckNumberExist(true, 200, true, generateToken)
                        )

                    } else {
                        call.respond(
                            HttpStatusCode.OK, CheckNumberExist(false, 200, false, "no access")
                        )
                    }
                } else {
                    call.respond(
                        Message(
                            "Phone number should not be empty", false, 200
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
        get("/ExclusiveOffers") {
            try {
                val pincode = call.parameters["pincode"]
                val product = db.getHomeAllProducts1(pincode ?: "")
                val exclusive: List<HomeProducts> = product.filter { it.productExclusiveSelling }
                apiListResponse(
                    HttpStatusCode.OK, statusCode = 200, ls = exclusive, message = "fetched successfully", status = true
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
        get("/getBannerCategory") {
            try {
                val pincode = call.parameters["pincode"]
                val product = db.getBannerCategory(pincode.toString())
                if (product.isNotEmpty()) apiListResponse(
                    HttpStatusCode.OK, statusCode = 200, ls = product, message = "fetched successfully", status = true
                )
                else {
                    apiListResponse(
                        HttpStatusCode.OK,
                        statusCode = 200,
                        ls = product,
                        message = "Please assign category",
                        status = false
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
        post("/ItemsCollections") {
            try {
                val requestBody = call.receive<SearchByProductId>()
                if (requestBody.ProductId != null) {
                    val listItems = db.getProductSubItems(requestBody.ProductId,requestBody.pincode)
                    apiListResponse(
                        HttpStatusCode.OK,
                        statusCode = 200,
                        ls = listItems,
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
                val pincode = call.parameters["pincode"]
                val product = db.getProductCategory(pincode.toString())
                if (product.isNotEmpty()) apiListResponse(
                    HttpStatusCode.OK, statusCode = 200, ls = product, message = "fetched successfully", status = true
                )
                else {
                    apiListResponse(
                        HttpStatusCode.OK,
                        statusCode = 200,
                        ls = product,
                        message = "Please assign category",
                        status = false
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
        post("/getAdminDetails"){
            val admins:List<adminAcess> =  db.getAllAdmins()
            val filterList: List<adminAvailable> = admins.map { adminAcess ->
                adminAvailable(adminAcess.pincode,adminAcess.price,adminAcess.city,adminAcess.deliveryContactNumber?:"")}

            call.respond(status = HttpStatusCode.OK, CommonListResponse(filterList.toList(), 200, "fetched successfully"))
        }
        post("/getUserDetails") {
            try {
                val requestBody = call.receive<UserRequest>()
                if (requestBody.phone != null) {
                    val user = db.getUserByPhone(requestBody.phone)
                    val order = db.getAllOrder("Ordered", requestBody.phone,requestBody.pincode)
                    val deliver = db.getAllOrder("Delivered", requestBody.phone,requestBody.pincode)
                    val cancel = db.getAllOrder("Cancelled", requestBody.phone,requestBody.pincode)
                    user?.cancel = cancel.size.toString()
                    user?.deliver = deliver.size.toString()
                    user?.order = order.size.toString()

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
                val product = db.getHomeAllProducts(
                    Integer.parseInt(skip),
                    Integer.parseInt(limit),
                    category?.split("__")?.get(0),
                    pincode = category?.split("__")?.get(1)
                )
                apiListResponse(
                    HttpStatusCode.OK, statusCode = 200, ls = product, message = "fetched successfully", status = true
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
                val pincode = call.parameters["pincode"]
                if (value?.isNotEmpty() == true) {
                    val regex = Regex("${value}.*", RegexOption.IGNORE_CASE)
                    val product = db.getSearchAllProducts(regex, pincode)
                    apiListResponse(
                        HttpStatusCode.OK,
                        statusCode = 200,
                        ls = product,
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
        get("/BestSelling") {
            try {
                val pincode = call.parameters["pincode"]
                val product = db.getHomeAllProducts1(pincode.toString())
                val best = product.filter { it.productBestSelling }
                apiListResponse(
                    HttpStatusCode.OK, statusCode = 200, ls = best, message = "fetched successfully", status = true
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
                if (product?.productId != null) call.respond(
                    status = HttpStatusCode.OK, ProductResponseById(product, 200, "fetched successfully")
                )
                else call.respond(
                    status = HttpStatusCode.OK, ProductResponseById(null, 400, "Items not available")
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
                val lsInput: ArrayList<Orders> = ArrayList()
                val cal = Calendar.getInstance()

                val dateFormat = SimpleDateFormat("dd/MM/yyyy")
                val timeFormat = SimpleDateFormat("HH:mm:ss")
                // Format the date from the Calendar instance
                val formattedDate = dateFormat.format(cal.time)
                val formattedTime = timeFormat.format(cal.time)
                requestBodyItem.createdDate = "$formattedDate $formattedTime"
                requestBodyItem.orderId = "OD${System.currentTimeMillis()}"
                requestBodyItem.orderStatus = "Ordered"

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
                            "Missing Item", false, 401
                        )
                    )
                }
            } catch (e: Exception) {
                apiResponse(
                    statusCode = 401,
                    message = "${e.message}",
                    status = false,
                    statusCodeApi = HttpStatusCode.BadRequest,
                )

            }
        }
        post("/AvailibilityCheck") {
            try {
                val pincode = call.receive<String>()
                if ((pincode.replace("\"", "") == "136027") || (pincode.replace(
                        "\"", ""
                    ) == "122018") || (pincode.replace("\"", "") == "122505")
                ) {
                    apiResponse(
                        HttpStatusCode.OK, statusCode = 200, message = "fetched successfully", status = true
                    )
                } else {
                    apiResponse(
                        HttpStatusCode.OK, statusCode = 201, message = "fetched successfully", status = true
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
                val request = call.receive<String>().replace("\"status\":","").replace("{","").replace("}","")
                val status= request.split("@")[0]
                val mobileNumber=request.split("@")[1]
                    val pincode=request.split("@")[2]
                print("status_mobile_pincode ${status} $mobileNumber $pincode")
                val orders = db.getAllOrder(status,mobileNumber,pincode.trim())
                apiListResponse(
                    HttpStatusCode.OK, statusCode = 200, ls = orders, message = "fetched successfully", status = true
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
                if (product?.productId != null) call.respond(
                    status = HttpStatusCode.OK, ProductResponseById(product, 200, "fetched successfully")
                )
                else call.respond(
                    status = HttpStatusCode.OK, ProductResponseById(null, 400, "Items not available")
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
                val product = db.getRelatedSearch(requestBody.pincode!!).take(4)
                apiListResponse(
                    HttpStatusCode.OK, statusCode = 200, ls = product, message = "fetched successfully", status = true
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
                if (product?.productId != null) call.respond(
                    status = HttpStatusCode.OK, ProductResponseById(product, 200, "fetched successfully")
                )
                else call.respond(
                    status = HttpStatusCode.OK, ProductResponseById(null, 400, "Items not available")
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
                val pincode = call.parameters["pincode"]
                val allCategory = db.getProductCategory(pincode.toString()).filter {
                    it.category != "Best Selling"
                }.filter { it.category != "exclusive" }.filter {
                    it.category != "best"
                }
                val product = db.getHomeAllProducts1(pincode.toString())
                val cat1: List<HomeProducts> = product.filter { mainCategory ->
                    allCategory.lastOrNull()?.subCategoryList?.getOrNull(0)?.name == mainCategory.item_subcategory_name.toString()
                }.take(4)
                val cat2: List<HomeProducts> = product.filter { mainCategory ->
                    allCategory.lastOrNull()?.subCategoryList?.getOrNull(1)?.name == mainCategory.item_subcategory_name.toString()
                }.take(4)
                val cat3: List<HomeProducts> = product.filter { mainCategory ->
                    allCategory.lastOrNull()?.subCategoryList?.getOrNull(2)?.name == mainCategory.item_subcategory_name.toString()
                }.take(4)
                val cat4: List<HomeProducts> = product.filter { mainCategory ->
                    allCategory.lastOrNull()?.subCategoryList?.getOrNull(3)?.name == mainCategory.item_subcategory_name.toString()
                }.take(4)
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
                    HttpStatusCode.OK, statusCode = 200, ls = ls, message = "fetched successfully", status = true
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
}

//}

