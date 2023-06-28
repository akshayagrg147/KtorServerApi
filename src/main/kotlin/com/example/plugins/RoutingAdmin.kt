package com.example.plugins


import com.example.TokenManager
import com.example.src.adminModalClass.LoginResponseBody
import com.example.src.adminModalClass.RequestLoginBody
import com.example.src.data.*
import com.example.src.repository.DatabaseFactory
import com.typesafe.config.ConfigFactory
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.config.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*


fun Route.adminRoute(
    db: DatabaseFactory
) {

    val tokenManager = TokenManager(HoconApplicationConfig(ConfigFactory.load()))
        route("/Admin") {
            get("/allProducts") {
                try {

                    val product = db.getHomeAllProducts()

                    call.respond(status = HttpStatusCode.OK, HomeProductResponse(product, 200, "fetched successfully"))

                } catch (e: Exception) {
                    application.log.error("Failed to get data", e.message)
                    call.respond("${e.message}")
                }
            }

        post ("/Login"){
            try {
                val value = call.receive<RequestLoginBody>()
                if(value.email=="akshaygarg147@gmail.com" && value.password=="123456789") {
                    call.respond(
                        status = HttpStatusCode.OK,
                        LoginResponseBody(status=true, statusCode = 200, message ="Successfully Logged In")
                    )
                }
                else
                {
                    call.respond(
                        status = HttpStatusCode.BadRequest,
                        LoginResponseBody(status=false, statusCode = 400, message ="Incorrect Information's")
                    )

                }
            } catch (e: Exception) {
                application.log.error("Failed to get data", e.message)
                call.respond(status = HttpStatusCode.BadRequest,"${e.message}")
            }
        }
        post ("/AddProduct"){
            try{
                var requestBody=call.receive<HomeProducts>()
                println("jdjjd  ${requestBody}")

                if(requestBody.productId.isNotEmpty()){
                    //BestSelling
                    if(requestBody.categoryType==1){
                        println("jdjjd  ${db.getLastProductId( )}")
                        var value=((db.getLastProductId()?.toInt() ?: 1)).toString()
                        if(value != "1")
                            value=(value.toInt()+1).toString()


                        val obj=bestSelling(productId = "")

                       obj.productName= requestBody.productName
                        obj.price= requestBody.price
                        obj.quantity= requestBody.quantity
                        obj.ProductImage1= requestBody.ProductImage1
                        obj.ProductImage2= requestBody.ProductImage2
                        obj.ProductImage3= requestBody.ProductImage3
                        obj.productId= value
                        obj.ProductDescription= requestBody.ProductDescription
                        obj.actual_price= requestBody.actual_price
                        obj.DashboardDisplay= requestBody.DashboardDisplay
                        obj.category= requestBody.category
                        obj.rating= requestBody.rating
                        obj.itemCategoryId= requestBody.itemCategoryId
                        obj.categoryType= requestBody.categoryType

                        val addProductClass=  db .addBestSellingAdminDashboard(obj)
                        call.respond(
                            status = HttpStatusCode.OK,
                         LoginResponseBody(status=true, statusCode = 200, message ="Added product Successfully")
                        )
                    }
                    //Exclusive
                    else if(requestBody.categoryType==2){
                       var value=((db.getLastProductId()?.toInt() ?: 1)).toString()
                        if(value != "1")
                            value=(value.toInt()+1).toString()

                        val obj=exclusiveOffers()
                        obj.productName= requestBody.productName
                        obj.price= requestBody.price
                        obj.quantity= requestBody.quantity
                        obj.ProductImage1= requestBody.ProductImage1
                        obj.ProductImage2= requestBody.ProductImage2
                        obj.ProductImage3= requestBody.ProductImage3
                        obj.productId= value
                        obj.ProductDescription= requestBody.ProductDescription
                        obj.actual_price= requestBody.actual_price
                        obj.DashboardDisplay= requestBody.DashboardDisplay
                        obj.category= requestBody.category
                        obj.rating= requestBody.rating
                        obj.itemCategoryId= requestBody.itemCategoryId
                        obj.categoryType= requestBody.categoryType
                        db .addExclusiveAdminDashboard(obj)
                         db .addProductAdminDashboard(requestBody)
                        call.respond(
                            status = HttpStatusCode.OK,
                            LoginResponseBody(status=true, statusCode = 200, message ="Added product Successfully")
                        )
                    }
                    //homeproduct
                    else if(requestBody.categoryType==3){

                        val addProductClass=  db .addProductAdminDashboard(requestBody)
                        call.respond(
                            status = HttpStatusCode.OK,
                            LoginResponseBody(status=true, statusCode = 200, message ="Added product Successfully")
                        )
                    }
                    //itemcollection
                    else{
                        var value=((db.getLastProductId()?.toInt() ?: 1)).toString()
                        if(value != "1")
                            value=(value.toInt()+1).toString()
                        val obj=itemsCollections()
                        obj.productName= requestBody.productName
                        obj.price= requestBody.price
                        obj.quantity= requestBody.quantity
                        obj.ProductImage1= requestBody.ProductImage1
                        obj.ProductImage2= requestBody.ProductImage2
                        obj.ProductImage3= requestBody.ProductImage3
                        obj.productId= value
                        obj.ProductDescription= requestBody.ProductDescription
                        obj.actual_price= requestBody.actual_price
                        obj.DashboardDisplay= requestBody.DashboardDisplay
                        obj.category= requestBody.category
                        obj.rating= requestBody.rating
                        obj.itemCategoryId= requestBody.itemCategoryId
                        obj.categoryType= requestBody.categoryType


                        val addProductClass=  db .addadditemsCollectionsAdminDashboard(obj)
                        call.respond(
                            status = HttpStatusCode.OK,
                            LoginResponseBody(status=true, statusCode = 200, message ="Added product Successfully")
                        )
                    }

                    requestBody.productId

                }
                else{
                    call.respond(
                        status = HttpStatusCode.BadRequest,
                        LoginResponseBody(status=false, statusCode = 200, message ="Missing Information's")
                    )
                }
            }
            catch (e:Exception){

            }
        }
            get("/RecentOrderCount") {
                try {
                val orders = db.getAllOrder()
                val ordersFilter=orders.take(10)
                    val product = db.getHomeAllProducts()
                    val users = db.getAlUsers()

                    call.respond(status = HttpStatusCode.OK, CountResponse(image = "https://ik.imagekit.io/00itvcwwk/default-image.jpg?updatedAt=1687762479357", name = "Akshay Garg",ordersFilter,listOf(ItemCount("ProductItems",product.size),ItemCount("Users",users.size),ItemCount("Orders",orders.size)), 200, "fetched successfully"))

                } catch (e: Exception) {
                    call.respond(status = HttpStatusCode.BadRequest, "${e.message}")
                }
            }
            get("/AllOrdersByPages") {
                try {
                    val skip = call.parameters["skip"]
                    val limit = call.parameters["limit"]
                    val orders = db.getAllOrderPagination(Integer.parseInt(skip),Integer.parseInt(limit))
                    call.respond(status = HttpStatusCode.OK, HomeProductResponse(orders, 200, "fetched successfully"))

                } catch (e: Exception) {
                    call.respond(status = HttpStatusCode.BadRequest, "${e.message}")
                }
            }
            delete("deleteProduct/{productId}") {
                val id = call.parameters["productId"]

                try {
                    val delete = db.deleteProductById(id!!)
                    if (delete) {
                        call.respond(
                            status = HttpStatusCode.OK,
                            LoginResponseBody(status=true, statusCode = 200, message ="Deleted product Successfully")
                        )
                        call.respond("user delete")
                    } else {
                        call.respond(
                            status = HttpStatusCode.OK,
                            LoginResponseBody(status=true, statusCode = 200, message ="Product not Deleted")
                        )
                    }

                } catch (e: Exception) {
                    call.respond(
                        status = HttpStatusCode.OK,
                        LoginResponseBody(status=false, statusCode = 401, message ="${e.message}")
                    )
                    application.log.error("Failed to get data", e.message)
                }
            }
            get("/AllUsers"){
                try {
                    val users = db.getAlUsers()
                    call.respond(status = HttpStatusCode.OK, HomeProductResponse(users, 200, "fetched successfully"))

                } catch (e: Exception) {
                    application.log.error("Failed to get data", e.message)
                    call.respond("${e.message}")
                }
            }

            post ("/getAddedPrd"){  }


    }


}
fun addAccessControlHeaders(call: ApplicationCall) {
    call.response.headers.append(HttpHeaders.AccessControlAllowOrigin, "*")
    call.response.headers.append(HttpHeaders.AccessControlAllowMethods, HttpMethod.Get.toString())
    call.response.headers.append(HttpHeaders.AccessControlAllowMethods, HttpMethod.Head.toString())
}



