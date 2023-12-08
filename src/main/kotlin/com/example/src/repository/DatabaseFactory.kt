package com.example.src.repository

import com.example.EmailData
import com.example.features.admin.domain.route.sendEmail
import com.example.features.customer.domain.modal.Users
import com.example.src.modal.*
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.bson.Document

import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import java.net.URLEncoder

class DatabaseFactory {
    private val username = URLEncoder.encode("akshaygarg147", "UTF-8")
    private val password = URLEncoder.encode("Akshaygarg147@", "UTF-8")
    private val url = "mongodb+srv://$username:$password@cluster0.qkvbexc.mongodb.net/"
    private val client = KMongo.createClient(url).coroutine
    private val database = client.getDatabase("groceryMain")
    val userCollection: CoroutineCollection<Users> = database.getCollection()
    val orderdetails: CoroutineCollection<orderitem> = database.getCollection()
    val home_collections: CoroutineCollection<HomeProducts> = database.getCollection()
    val adminAcessCollection: CoroutineCollection<adminAcess> = database.getCollection()
   val exclusiveCollection: CoroutineCollection<exclusiveOffers> = database.getCollection()
    val bestSellngCollection: CoroutineCollection<bestSelling> = database.getCollection()
    val adminItemCategory: CoroutineCollection<ProductCategory> = database.getCollection()
    val adminBannerCategory: CoroutineCollection<BannerCategory> = database.getCollection()
    val addCouponRequest:CoroutineCollection<AddCouponRequest> = database.getCollection()
    val allCoupons:CoroutineCollection<AddCouponRequest> = database.getCollection()

    suspend fun addCategory(request: ProductCategory): ProductCategory {
        adminItemCategory.insertOne(request)
        return request
    }
    suspend fun addBannerCategory(request: BannerCategory): BannerCategory {
        adminBannerCategory.insertOne(request)
        return request
    }
    suspend fun addCoupon(request:AddCouponRequest):AddCouponRequest{
        addCouponRequest.insertOne(request)
        return request
    }



    suspend fun getProductCategory(pincode:String,sellerId:String?=null): List<ProductCategory> {
        return adminItemCategory.find(ProductCategory::pincode eq pincode.replace("\"", ""),if(sellerId?.isNotEmpty()==true)ProductCategory::sellerId eq sellerId.replace("\"", "") else null).toList()
    }
    suspend fun getBannerCategory(pincode:String): List<BannerCategory> {
        return adminBannerCategory.find(ProductCategory::pincode eq pincode.replace("\"", "")).toList()
    }

    suspend fun addProductAdminDashboard(request: HomeProducts): HomeProducts {
        home_collections.insertOne(request)
        return request
    }

    suspend fun addExclusiveAdminDashboard(request: exclusiveOffers): exclusiveOffers {
        exclusiveCollection.insertOne(request)
        return request
    }

    suspend fun addBestSellingAdminDashboard(request: bestSelling): bestSelling {
        bestSellngCollection.insertOne(request)
        return request
    }

    suspend fun addUser(users: Users): Users {
        userCollection.insertOne(users)
        return users
    }

    suspend fun orderdetails(order: orderitem): orderitem {
        // Assuming `orderdetails` is a MongoDB collection
        orderdetails.insertOne(order)

        val fcmTokens: List<adminAcess> = order.listOfSellerId?.flatMap { sellerId ->
            adminAcessCollection.find(
                and(
                    adminAcess::pincode eq order.pincode,
                    adminAcess::sellerId eq sellerId
                )
            ).toList()
        } ?: emptyList()

        order.sellerId = ""
        order.fcm_tokenSeller = ArrayList()
        order.fcm_tokenSeller.addAll(fcmTokens.map { it.fcm_token.toString() })
        return order

    }


    suspend fun getAllOrder(status:String,mobileNumber:String?=null,pincode:String?=null,sellerId:String?=null): List<orderitem> {

       return orderdetails.find(orderitem::orderStatus eq status.replace("\"", ""),if(sellerId?.isNotEmpty()==true)orderitem::fcm_tokenSeller contains sellerId.replace("\"", "") else null,if(pincode?.isNotEmpty()==true)orderitem::pincode eq pincode.replace("\"", "") else null).toList()

    }
    suspend fun getAllOrderPagination(skip: Int?, limit: Int?,pincode: String,sellerId:String): List<orderitem> =
        orderdetails.find(orderitem::pincode eq pincode.replace("\"", ""),orderitem::sellerId eq sellerId.replace("\"", "")).skip(skip ?: 0).limit(limit ?: 0).toList()

    suspend fun getAllUsers(): List<Users> = userCollection.find().toList()


    //get home products
    suspend fun getSearchAllProducts(string: Regex, pincode: String?,sellerId:String?=null): List<HomeProducts> =
        home_collections.find(HomeProducts::productName regex string,HomeProducts::pincode eq pincode,if(sellerId?.isNotEmpty()==true)HomeProducts::sellerId eq sellerId.replace("\"", "") else null).toList()

    suspend fun getHomeAllProducts(offset: Int? = 0, limit: Int? = 0, category: String? = "",pincode: String?,sellerId:String?=null): List<HomeProducts> =
        home_collections.find(if(category!=null)HomeProducts::item_subcategory_name eq category.replace("\"", "") else null,HomeProducts::pincode eq pincode?.replace("\"", ""),if(sellerId?.isNotEmpty()==true)HomeProducts::sellerId eq sellerId.replace("\"", "") else null).skip(offset ?: 0).limit(limit ?: 0).toList()

    suspend fun GetPendingProductById(productId: String): HomeProducts? =
        home_collections.find(HomeProducts::productId eq productId).first()

    suspend fun getLastProductId(): String? =
        home_collections.find().toList()[home_collections.find().toList().size - 1].productId

    suspend fun getItemCount(): Int = home_collections.find().toList().size - 1
    suspend fun getExclusiveProductBasedId(productId: String): HomeProducts? =
        home_collections.find(HomeProducts::productId eq productId).first()

    suspend fun getBestProductBasedId(productId: String): HomeProducts? =
        home_collections.find(HomeProducts::productId eq productId).first()

    suspend fun getAllCoupons(pincode: String?): List<AddCouponRequest>? =
        allCoupons.find(AddCouponRequest::pincode eq pincode).toList()


    suspend fun getRelatedSearch(pincode: String): List<HomeProducts> = home_collections.find(HomeProducts::pincode eq pincode).toList()


    suspend fun getAllExclusiveCollection(): List<exclusiveOffers> =
        exclusiveCollection.find().toList()

    suspend fun getHomeAllProducts1(pincode:String,sellerId:String?=null): List<HomeProducts> =
        home_collections.find(HomeProducts::pincode eq pincode,if(sellerId?.isNotEmpty()==true)HomeProducts::sellerId eq sellerId.replace("\"", "") else null).toList()

    suspend fun getAlUsers(): List<Users> = userCollection.find().toList()

    suspend fun getAllAdmins(): List<adminAcess> = adminAcessCollection.find().toList()

    suspend fun setOrderStatus(req: orderitem,sendEmail:(String)->Unit): Long {
        val update = Document(
            "\$set",
            Document("orderId", req.orderId)

                .append("totalOrderValue", req.totalOrderValue)

                .append("orderList", req.orderList)
                .append("address", req.address)
                .append("createdDate", req.createdDate)
                .append("mobilenumber", req.mobilenumber)
                .append("paymentmode", req.paymentmode)
                .append("pincode", req.pincode)
                .append("changeTime", req.changeTime)
                .append("orderStatus", req.orderStatus)

        )


        val result = orderdetails.updateOne(Document("orderId", req.orderId), update)
        if(result.modifiedCount>0){
            val obj= userCollection.find(( Users::phone eq req.mobilenumber.replace("+",""))).first()
          //  sendNotification(obj?.fcmtoken?:"","order ${req.orderStatus} ","check your orders")
            //sendEmail(obj?.email?:"")
        }


        return result.modifiedCount
    }



    suspend fun getProductSubItems(productId: String, pincode: String?): List<HomeProducts?> =
        home_collections.find(HomeProducts::item_subcategory_name eq productId,HomeProducts::pincode eq pincode).toList()

    suspend fun getUserByPhone(phone: String): Users? = userCollection.find(Users::phone eq phone.replace("\"", "")).first()
    suspend fun checkNumberExist(phone: String): Boolean =
        userCollection.find(Users::phone eq phone).toList().isNotEmpty()

    suspend fun deleteUserById(userId: String): Boolean =
        userCollection.deleteOne(Users::userId eq userId).wasAcknowledged()

    suspend fun userCheck(email:String,passord:String):adminAcess =
        adminAcessCollection.find(( adminAcess::password eq passord)).first()!!

    suspend fun freeDeliveryPriceUpdateUpdate(
        email: String,
        passord: String,
        name: String,
        pincode: String,
        price: String,
        fcm: String,
        deliveryContactNumber: String,
        city: String,
        sellerId: String?

    ):Long {
        val update = Document(
            "\$set",
            Document("email", email)
                .append("password", passord)
                .append("pincode", pincode)
                .append("name", name)
                .append("price", price)
                .append("fcm_token", fcm)
                .append("deliveryContactNumber", deliveryContactNumber)
                .append("city", city)
                .append("sellerId", sellerId)


        )

        val result = adminAcessCollection.updateOne(Document("email", email), update)
        return result.modifiedCount
    }



    suspend fun deleteProductById(productId: String): Boolean =
        home_collections.deleteOne(HomeProducts::productId eq productId).wasAcknowledged()

    suspend fun deleteCategory(categoryName: String): Boolean =
        adminItemCategory.deleteOne(ProductCategory::category eq categoryName).wasAcknowledged()

    suspend fun deleteBannerategory(categoryName: String): Boolean =
        if(categoryName.split("__")[1] == "bannercategory1")
        adminItemCategory.deleteOne(BannerCategory::bannercategory1 eq categoryName).wasAcknowledged()
    else
            adminItemCategory.deleteOne(BannerCategory::bannercategory2 eq categoryName).wasAcknowledged()

//    suspend fun deleteProductsBasedCategory(categoryName: String): Boolean =
//        home_collections.deleteMany(HomeProducts:: eq categoryName).wasAcknowledged()
    suspend fun deleteCoupon(couponName: String): Boolean = allCoupons.deleteOne(AddCouponRequest::couponCode eq couponName).wasAcknowledged()



    suspend fun updateProduct(req: HomeProducts): Long {
        val update = Document(
            "\$set",
            Document("productName", req.productName)
                .append("orignal_price", req.orignal_price)
                .append("quantity", req.quantity)
                .append("productId", req.productId)
                .append("selling_price", req.selling_price)
                .append("dashboardDisplay", req.dashboardDisplay)
                .append("item_category_name", req.item_category_name)
                .append("item_subcategory_name", req.item_subcategory_name)
                .append("productDescription", req.productDescription)
                .append("productImage2", req.productImage2)
                .append("productImage1", req.productImage1)
                .append("productImage3", req.productImage3)
                .append("productImage3", req.productImage3)
                .append("productImage3", req.productImage3)
                .append("productBestSelling", req.productBestSelling)
                .append("quantityInstructionController", req.quantityInstructionController)
                .append("productExclusiveSelling", req.productExclusiveSelling)
                .append("pincode",req.pincode)
        )

        val result = home_collections.updateOne(Document("productId", req.productId), update)
        return result.modifiedCount
    }

    suspend fun updateBannerCategory(req: BannerCategory): Long {
        try {
            val update = Document(
                "\$set",
                Document("bannercategory1", req.bannercategory1)
                    .append("imageUrl1", req.imageUrl1)
                    .append("bannercategory2", req.bannercategory2)
                    .append("imageUrl2", req.imageUrl2)
                    .append("bannercategory3", req.bannercategory3)
                    .append("imageUrl3", req.imageUrl3)
                    .append("pincode", req.pincode)
                    .append("changetime", req.changetime)
                    .append("subCategoryList", req.subCategoryList?.map { subCategory ->
                        Document()
                            .append("name", subCategory.name)
                            .append("subCategoryUrl", subCategory.subCategoryUrl)
                    })
            )

            val result = adminBannerCategory.updateOne(Document("bannercategory1", req.bannercategory1), update)

            return result.modifiedCount
        } catch (e: Exception) {
            print("update_banner_Error ${e.message}")
            // Handle exceptions here (e.g., log the error).
            // You may want to rethrow the exception or return an error code.
            e.printStackTrace()
            return 0L
        }
    }


    suspend fun registerCustomertoken(token: String, mobile: String): Long {
        print("registerCustomertoken1 $token $mobile")

        val obj= userCollection.find(( Users::phone eq mobile)).first()
        val update = Document(
            "\$set",
            Document("email", obj?.email)

                .append("name", obj?.name)

                .append("phone", obj?.phone)
                .append("profileImage", obj?.profileImage)
                .append("fcmtoken", token)
                .append("changetime",System.currentTimeMillis().toDouble())


        )

        val result = userCollection.updateOne(Document("phone", obj?.phone), update)
        return result.modifiedCount
    }
    fun sendNotification(token: String, title: String, body: String) {
        val message = Message.builder()
            .putData("title", title)
            .putData("body", body)
            .setToken(token)
            .build()

        val response = FirebaseMessaging.getInstance().send(message)

        // Handle the response, check for errors, etc.
        println("Successfully sent message: $response")
    }








}