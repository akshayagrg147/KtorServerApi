package com.example.features.admin.domain.route


import com.example.EmailData
import com.example.generateToken
import com.example.generateTokenAdmin
import com.example.jwtConfig

import com.example.src.modal.*
import com.example.src.repository.DatabaseFactory
import com.example.utils.apiClassResponse
import com.example.utils.apiListResponse
import com.example.utils.apiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.commons.mail.DefaultAuthenticator
import org.apache.commons.mail.SimpleEmail
import java.text.SimpleDateFormat
import java.util.*

fun Route.adminRoute(
    db: DatabaseFactory
) {
    route("/Admin") {
        post("/OrderStatus") {

            val requestBody = call.receive<orderitem>()
            requestBody.changeTime = System.currentTimeMillis().toDouble()
            if (requestBody.orderStatus?.isNotEmpty() == true) {

                if (db.setOrderStatus(requestBody) {
                        if (it.isNotEmpty()) {
                            val result = sendEmail(
                                to = it,
                                subject = "Order Status: ${requestBody.orderStatus}",
                                body = "Dear customer,\n\nYour order status is: ${requestBody.orderStatus}.",

                                smtpHost = "smtp.gmail.com",
                                smtpPort = 587, // or your SMTP port
                                smtpUsername = "akshaygarg147@gmail.com",
                                smtpPassword = "crrm ddex jwxo fmco"
                            )

                            if (result) {
                                print("Email sent successfully.")
                            } else {
                                print("Failed to send email.")
                            }

                        }
                    } > 0) {
                    call.respond(
                        status = HttpStatusCode.OK,
                        ApiResponse(status = true, statusCode = 200, message = "Updated status Successfully")
                    )


                } else {
                    call.respond(
                        status = HttpStatusCode.BadRequest,
                        ApiResponse(status = false, statusCode = 400, message = "please check request body")
                    )
                }
            } else {
                call.respond(
                    status = HttpStatusCode.OK,
                    ApiResponse(status = false, statusCode = 200, message = "Incorrect status")
                )
            }
        }
        post("/allCategoryItemsCollections") {
            try {
                val requestBody = call.receive<SearchByProductId>()
                var listItems= mutableListOf<HomeProducts?>()
                if (requestBody.combineCategory != null) {
                    val resultList = requestBody.combineCategory?.split("_")

                    if (resultList != null) {
                        for(category in resultList){
                            listItems.addAll( db.getProductAllSubItems(category, requestBody.sellerId))
                        }
                    }

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
        post("/send-email") {
            val emailData = call.receive<EmailData>()
            val result = sendEmail(
                to = emailData.to,
                subject = emailData.subject,
                body = emailData.body,
                smtpHost = "smtp.gmail.com",
                smtpPort = 587, // or your SMTP port
                smtpUsername = "akshaygarg147@gmail.com",
                smtpPassword = "crrm ddex jwxo fmco"
            )

            if (result) {
                call.respond(HttpStatusCode.OK, "Email sent successfully.")
            } else {
                call.respond(HttpStatusCode.InternalServerError, "Failed to send email.")
            }
        }

        post("/Login") {
            try {
                val user = call.receive<RequestLoginBody>()
                val product = db.userCheck(user.email, user.password)
                if (product.pincode != null) {

                    val generateToken = generateTokenAdmin(
                        user, jwtConfig
                    )
                    product.generateToken=generateToken
                    apiClassResponse(
                        statusCode = 200,
                        message = "Successfully Logged In",
                        status = true,
                        response = product,
                        statusCodeApi = HttpStatusCode.OK,
                    )



                } else {
                    apiClassResponse(
                        statusCode = 400,
                        message = " Logged failed",
                        status = true,
                        response = adminAcess(null, null, null),
                        statusCodeApi = HttpStatusCode.BadRequest,
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


            post("/updateProduct") {
                val requestBody = call.receive<HomeProducts>()
                requestBody.changeTime = System.currentTimeMillis().toDouble()
                if (db.updateProduct(requestBody) > 0) {
                    call.respond(
                        status = HttpStatusCode.OK,
                        ApiResponse(status = true, statusCode = 200, message = "Updated Product Successfully")
                    )
                } else {
                    call.respond(
                        status = HttpStatusCode.OK,
                        ApiResponse(status = false, statusCode = 200, message = "Incorrect product Id")
                    )
                }

            }
            post("/freeDelivery") {
                val requestBody = call.receive<adminAcess>()
                if (db.freeDeliveryPriceUpdateUpdate(
                        requestBody.email!!,
                        requestBody.password!!,
                        requestBody.name!!,
                        requestBody.pincode!!,
                        requestBody.price!!,
                        requestBody.fcm_token!!,
                        requestBody.deliveryContactNumber!!,
                        requestBody.city!!,
                    requestBody.sellerId
                    ) > 0
                ) {
                    call.respond(
                        status = HttpStatusCode.OK,
                        ApiResponse(status = true, statusCode = 200, message = "Updated Delivery Amount Successfully")
                    )
                } else {
                    call.respond(
                        status = HttpStatusCode.OK,
                        ApiResponse(status = false, statusCode = 200, message = "something went wrong")
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
                    apiResponse(
                        statusCode = 400,
                        message = "${e.message}",
                        status = false,
                        statusCodeApi = HttpStatusCode.BadRequest,
                    )

                }
            }
            delete("deleteCategory/{categoryName}") {
                val id = call.parameters["categoryName"]
                try {
                    val delete = db.deleteCategory(id!!)
                    if (delete) {
                        call.respond(
                            status = HttpStatusCode.OK,
                            ApiResponse(status = true, statusCode = 200, message = "Deleted category Successfully")
                        )

                    } else {
                        call.respond(
                            status = HttpStatusCode.OK,
                            ApiResponse(status = true, statusCode = 200, message = "category not Deleted")
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
            delete("deleteBanner/{categoryName}") {
                val id = call.parameters["categoryName"]
                try {
                    val delete = db.deleteBannerategory(id!!)
                    if (delete) {
                        call.respond(
                            status = HttpStatusCode.OK,
                            ApiResponse(status = true, statusCode = 200, message = "Deleted category Successfully")
                        )
                    } else {
                        call.respond(
                            status = HttpStatusCode.OK,
                            ApiResponse(status = true, statusCode = 200, message = "category not Deleted")
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
            get("/AllOrdersByPages") {
                try {
                    val pinCode = call.parameters["pincode"]
                    val sellerId = call.parameters["sellerId"]
                    val skip = call.parameters["skip"]
                    val limit = call.parameters["limit"]
                    val orders =
                        db.getAllOrderPagination(Integer.parseInt(skip), Integer.parseInt(limit), pinCode.toString(),sellerId.toString())
                    apiListResponse(
                        HttpStatusCode.OK,
                        statusCode = 200,
                        ls = orders,
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
                    val pincode = call.parameters["pincode"]
                    val sellerId = call.parameters["sellerId"]
                    if (value?.isNotEmpty() == true) {
                        val regex = Regex("${value}.*", RegexOption.IGNORE_CASE)
                        val product = db.getSearchAllProducts(regex, pincode,sellerId)
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
            get("/allProducts") {
                try {
                    val skip = call.parameters["skip"]
                    val limit = call.parameters["limit"]
                    val pincode = call.parameters["pincode"]
                    val sellerId = call.parameters["sellerId"]
                    val product = db.getHomeAllProducts(
                        Integer.parseInt(skip), Integer.parseInt(limit), null, pincode = pincode,sellerId
                    )
                    apiListResponse(
                        HttpStatusCode.OK,
                        statusCode = 200,
                        ls = product,
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
            delete("deleteCoupon/{couponName}") {
                val name = call.parameters["couponName"]
                try {
                    if (name?.isNotEmpty() == true) {
                        val isCouponDeleted = db.deleteCoupon(name)
                        if (isCouponDeleted) apiResponse(
                            statusCode = 200,
                            message = "Deleted Coupon",
                            status = true,
                            statusCodeApi = HttpStatusCode.OK,
                        )
                        else {
                            apiResponse(
                                statusCode = 400,
                                message = "Coupon code not exist",
                                status = true,
                                statusCodeApi = HttpStatusCode.BadRequest,
                            )
                        }
                    } else {
                        apiResponse(
                            statusCode = 200,
                            message = "Incorrect Information's",
                            status = false,
                            statusCodeApi = HttpStatusCode.OK
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
            get("/allCoupons") {
                try {
                    val pincode = call.parameters["pincode"]
                    val couponResponse = db.getAllCoupons(pincode)
                    val currentDateString = SimpleDateFormat("yyyy-MM-dd").format(Date())
                    // Compare the two date strings
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd")
                    val filterCoupons = couponResponse?.filter { coupon ->
                        val inputDate = dateFormat.parse(coupon.expireDate)
                        inputDate > dateFormat.parse(currentDateString)
                    }
                    apiListResponse(
                        HttpStatusCode.OK,
                        statusCode = 200,
                        ls = filterCoupons ?: emptyList(),
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
            post("/AddCoupon") {
                try {
                    val couponRequest = call.receive<AddCouponRequest>()
                    if (couponRequest.couponCode.isNotEmpty() && couponRequest.couponTitle.isNotEmpty() &&
//                    couponRequest.discountPercentage.isNotEmpty()&&
//                    couponRequest.discountedAmount.isNotEmpty()&&
                        couponRequest.minimumPurchase.isNotEmpty() && couponRequest.startDate.isNotEmpty() && couponRequest.expireDate.isNotEmpty()
                    ) {
                        if (couponRequest.startDate != couponRequest.expireDate) {
                            db.addCoupon(couponRequest)
                            apiResponse(
                                statusCode = 200,
                                message = "Added coupon Successfully",
                                status = true,
                                statusCodeApi = HttpStatusCode.OK,
                            )
                        } else {
                            apiResponse(
                                statusCode = 400,
                                message = "coupon Code can not be same",
                                status = true,
                                statusCodeApi = HttpStatusCode.BadRequest,
                            )
                        }

                    } else {
                        apiResponse(
                            statusCode = 400,
                            message = "Incorrect Information's",
                            status = false,
                            statusCodeApi = HttpStatusCode.BadRequest
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
            post("/AddProduct") {
                try {
                    val requestBody = call.receive<HomeProducts>()
                    //BestSelling
                    val specialCategories = listOf("best", "exclusive", "Essential Products")
                    if (specialCategories.any {
                            requestBody.item_category_name?.contains(
                                it,
                                ignoreCase = true
                            ) == true
                        }) {
                        if (db.getItemCount() <= 0) {
                            requestBody.productId = 1.toString()
                        } else {
                            var value = ((db.getLastProductId()?.toInt() ?: 1)).toString()
                            value = (value.toInt() + 1).toString()
                            requestBody.productId = value
                        }
                        val obj = bestSelling().apply {
                            productName = requestBody.productName
                            selling_price = requestBody.selling_price.toString()
                            quantity = requestBody.quantity
                            ProductImage1 = requestBody.productImage1
                            ProductImage2 = requestBody.productImage2
                            ProductImage3 = requestBody.productImage3
                            productId = requestBody.productId
                            ProductDescription = requestBody.productDescription
                            orignal_price = requestBody.orignal_price
                            dashboardDisplay = requestBody.dashboardDisplay
                            rating = requestBody.rating
                            item_category_name = requestBody.item_category_name
                        }

                        if (requestBody.item_category_name?.contains("best") == true) db.addBestSellingAdminDashboard(
                            obj
                        )
                        else {
                            val exclusive = exclusiveOffers().apply {
                                productName = requestBody.productName
                                selling_price = requestBody.selling_price.toString()
                                quantity = requestBody.quantity
                                ProductImage1 = requestBody.productImage1
                                ProductImage2 = requestBody.productImage2
                                ProductImage3 = requestBody.productImage3
                                productId = requestBody.productId
                                ProductDescription = requestBody.productDescription
                                orignal_price = requestBody.orignal_price
                                dashboardDisplay = requestBody.dashboardDisplay
                                rating = requestBody.rating
                                item_category_name = requestBody.item_category_name
                            }
                            db.addExclusiveAdminDashboard(exclusive)
                        }
                        db.addProductAdminDashboard(requestBody)
                        call.respond(
                            status = HttpStatusCode.OK,
                            ApiResponse(status = true, statusCode = 200, message = "Added product Successfully")
                        )
                    }
                    //itemcollection
                    else {
                        if (db.getItemCount() <= 0) {
                            requestBody.productId = 1.toString()
                        } else {
                            var value = ((db.getLastProductId()?.toInt() ?: 1)).toString()
                            value = (value.toInt() + 1).toString()
                            requestBody.productId = value
                        }
                        db.addProductAdminDashboard(requestBody)
                        apiResponse(
                            statusCode = 200,
                            message = "Added product Successfully",
                            status = true,
                            statusCodeApi = HttpStatusCode.OK,
                        )

                    }
                    requestBody.productId


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
                    val sellerId = call.parameters["sellerId"]
                    val product = db.getProductCategory(pincode.toString(),sellerId.toString())
                    apiListResponse(
                        HttpStatusCode.OK,
                        statusCode = 200,
                        ls = product,
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
            get("/getBannerCategory") {
                try {
                    val pincode = call.parameters["pincode"]
                    val sellerId = call.parameters["sellerId"]
                    val product = db.getBannerCategory(pincode.toString())
                    print("getbnnercategoryis ${product.size}  ${product}  ")
                    apiListResponse(
                        HttpStatusCode.OK,
                        statusCode = 200,
                        ls = product,
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
            post("/AddItemCategory") {
                try {
                    val requestBody = call.receive<ProductCategory>()
                    db.addCategory(requestBody)
                    call.respond(
                        status = HttpStatusCode.OK,
                        Message(statusCode = 200, message = "Added successfully", status = true)
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
            post("/AddBannerCategory") {
                try {
                    val requestBody = call.receive<BannerCategory>()
                    db.addBannerCategory(requestBody)
                    call.respond(
                        status = HttpStatusCode.OK,
                        Message(statusCode = 200, message = "Added successfully", status = true)
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
            post("/UpdateBannerCategory") {
                try {
                    val requestBody = call.receive<BannerCategory>()
                    requestBody.changetime = System.currentTimeMillis().toString()
                    val response = db.updateBannerCategory(requestBody)
                    if (response > 0) call.respond(
                        status = HttpStatusCode.OK,
                        Message(statusCode = 200, message = "UPDATE successfully", status = true)
                    )
                    else {
                        call.respond(
                            status = HttpStatusCode.OK,
                            ApiResponse(status = false, statusCode = 400, message = "Not updated")
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
            get("/RecentOrderCount") {
                try {
                    val pincode = call.parameters["pincode"]
                    val sellerId = call.parameters["sellerId"]
                    val orders = db.getAllOrder(status="Ordered",mobileNumber= null, pincode = pincode,sellerId=sellerId.toString())
                    val ordersFilter = orders.take(10)
                    val product = db.getHomeAllProducts1(pincode = pincode ?: "",sellerId=sellerId.toString())
                    val users = db.getAlUsers()
                    val category = db.getProductCategory(pincode ?: "",sellerId=sellerId.toString())
                    call.respond(
                        status = HttpStatusCode.OK, CountResponse(
                            image = "https://ik.imagekit.io/00itvcwwk/Products/image_1694930617258_iwJuKEKtx.png?updatedAt=1694930621116",
                            name = "Akshay Garg",
                            ordersFilter,
                            listOf(
                                ItemCount(
                                    "ProductItems",
                                    "https://ik.imagekit.io/00itvcwwk/Dashboard_Admin/MicrosoftTeams-image__20_.png?updatedAt=1689506162992",
                                    product.size
                                ), ItemCount(
                                    "Users",
                                    "https://ik.imagekit.io/00itvcwwk/Dashboard_Admin/MicrosoftTeams-image__22_.png?updatedAt=1689506163196",
                                    users.size
                                ), ItemCount(
                                    "Orders",
                                    "https://ik.imagekit.io/00itvcwwk/Dashboard_Admin/MicrosoftTeams-image__19_.png?updatedAt=1689506163736",
                                    orders.size
                                ), ItemCount(
                                    "Category",
                                    "https://ik.imagekit.io/00itvcwwk/Dashboard_Admin/MicrosoftTeams-image__21_.png?updatedAt=1689506163236",
                                    category.size
                                )
                            ),
                            200,
                            "fetched successfully"
                        )
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

            get("/deleteImageIoFile") {
                val id = call.parameters["fileId"]
                val client = OkHttpClient()
                val mediaType = "text/plain".toMediaType()
                val body = "".toRequestBody(mediaType)
                val request = Request.Builder()
                    .url("https://api.imagekit.io/v1/files/$id")
                    .method("DELETE", body)
                    .addHeader("Authorization", "Basic cHJpdmF0ZV9tdHVMdjFGa0YrVE9YbFV5SC9ZbEIvQkpndVE9Og==")
                    .build()
                val response = client.newCall(request).execute()
                print("response_from_imagekit${response}")
            }
            get("/AllUsers") {
                try {
                    val users = db.getAlUsers()
                    apiListResponse(
                        HttpStatusCode.OK, statusCode = 200, ls = users, message = "fetched successfully", status = true
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


    }



}
 fun sendEmail(
    to: String,
    subject: String,
    body: String,
    smtpHost: String,
    smtpPort: Int,
    smtpUsername: String,
    smtpPassword: String
): Boolean {
    return try {
        val email = SimpleEmail()
        email.setHostName(smtpHost)
        email.setSmtpPort(smtpPort)
        email.setAuthenticator(DefaultAuthenticator(smtpUsername, smtpPassword))
        email.isStartTLSEnabled = true

        email.setFrom(smtpUsername)
        email.addTo(to)
        email.subject = subject
        email.setMsg(body)

        email.send()

        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}




