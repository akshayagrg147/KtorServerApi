package com.example.src.repository

import com.example.features.admin.domain.modal.AddCouponRequest
import com.example.features.customer.domain.modal.Users
import com.example.src.modal.*
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import org.bson.Document

import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

class DatabaseFactory {
    private val username = URLEncoder.encode("akshaygarg147", "UTF-8")
    private val password = URLEncoder.encode("Akshaygarg147@", "UTF-8")
    private val url = "mongodb+srv://$username:$password@cluster0.qkvbexc.mongodb.net/"
    private val client = KMongo.createClient(url).coroutine
    private val database = client.getDatabase("groceryMain")
    val userCollection: CoroutineCollection<Users> = database.getCollection()
    val orderdetails: CoroutineCollection<OrderItem> = database.getCollection()
    val orderSummaryGraph: CoroutineCollection<OrderItemBarGraph> = database.getCollection()
    val home_collections: CoroutineCollection<HomeProducts> = database.getCollection()
    val adminAcessCollection: CoroutineCollection<adminAcess> = database.getCollection()
    val exclusiveCollection: CoroutineCollection<ExclusiveOffers> = database.getCollection()
    val bestSellngCollection: CoroutineCollection<BestSelling> = database.getCollection()
    val adminItemCategory: CoroutineCollection<ProductCategory> = database.getCollection()
    val adminBannerCategory: CoroutineCollection<BannerCategory> = database.getCollection()
    val addCouponRequest: CoroutineCollection<AddCouponRequest> = database.getCollection()
    val allCoupons: CoroutineCollection<AddCouponRequest> = database.getCollection()

    suspend fun addCategory(request: ProductCategory): ProductCategory {
        adminItemCategory.insertOne(request)
        return request
    }

    suspend fun addBannerCategory(request: BannerCategory): BannerCategory {
        adminBannerCategory.insertOne(request)
        return request
    }

    suspend fun addCoupon(request: AddCouponRequest): AddCouponRequest {
        addCouponRequest.insertOne(request)
        return request
    }


    suspend fun getProductCategory(pincode: String, sellerId: String? = null): List<ProductCategory> {
        return adminItemCategory.find(
            ProductCategory::society_pincode eq pincode.replace("\"", ""),
            if (sellerId?.isNotEmpty() == true) ProductCategory::sellerId eq sellerId.replace("\"", "") else null
        ).toList()
    }

    suspend fun getProductCategoryWise(pincode: String, sellerId: String? = null): List<HomeProducts> {
        return home_collections.find(HomeProducts::society_pincode eq pincode.replace("\"", "")).toList()
    }

    suspend fun updateColumnName(): Long {
        // No filter; this will apply to all documents in the collection
        val query = org.litote.kmongo.EMPTY_BSON

        // Use the $rename operator to rename the "pincode" field to "society_pincode"
        val update = Document("\$rename", Document("pincode", "society_pincode"))

        // Apply the update to rename the field in all documents
        val updateResult = adminAcessCollection.updateMany(query, update)

        // Return the count of documents that were modified
        return updateResult.modifiedCount
    }

    suspend fun updateSocietyPincode(): Long {
        // No filter; this will apply to all documents in the collection
        val query = org.litote.kmongo.EMPTY_BSON

        // Use the $set operator to update the "society_pincode" field for all documents
        val update = Document("\$set", Document("society_pincode", "sector 95a,Roselia"))

        // Apply the update to set the new value in all documents
        val updateResult = adminAcessCollection.updateMany(query, update)

        // Return the count of documents that were modified
        return updateResult.modifiedCount
    }

    suspend fun getAllProductCategory(sellerId: String? = null): List<ProductCategory> {
        return adminItemCategory.find(
            if (sellerId?.isNotEmpty() == true) ProductCategory::sellerId eq sellerId.replace(
                "\"",
                ""
            ) else null
        ).toList()
    }

    suspend fun getBannerCategory(pincode: String): List<BannerCategory> {
        return adminBannerCategory.find(ProductCategory::society_pincode eq pincode.replace("\"", "")).toList()
    }

    suspend fun addProductAdminDashboard(request: HomeProducts): HomeProducts {
        home_collections.insertOne(request)
        return request
    }

    suspend fun addExclusiveAdminDashboard(request: ExclusiveOffers): ExclusiveOffers {
        exclusiveCollection.insertOne(request)
        return request
    }

    suspend fun addBestSellingAdminDashboard(request: BestSelling): BestSelling {
        bestSellngCollection.insertOne(request)
        return request
    }

    suspend fun addUser(users: Users): Users {
        userCollection.insertOne(users)
        return users
    }


    suspend fun orderdetails(order: OrderItem): OrderItem {
        val orderTemp = order
        var combineOrderId = ""
        val fcmAdded = mutableListOf<String>()
        val groupedOrders = order.orderList.groupBy { it.sellerIdName }
        val orderDate = order.createdDate.split(" ")[0]

        for ((sellerId, orders) in groupedOrders) {
            orders.forEach { eachOrderDetail ->
                if (sellerId != null) {
                    // Update order summary graph
                    val fetchData = orderSummaryGraph.find(
                        OrderItemBarGraph::sellerId eq sellerId,
                        OrderItemBarGraph::createdDate eq orderDate
                    )
                    val newQuantityGraph = fetchData.first()?.quantity?.plus(1) ?: 1

                    val filter = Document(
                        "\$and", listOf(
                            Document("sellerId", sellerId),
                            Document("createdDate", orderDate)
                        )
                    )
                    val summaryUpdate = Document(
                        "\$set", Document("createdDate", orderDate)
                            .append("pincode", order.society_pincode)
                            .append("sellerId", sellerId)
                            .append("quantity", newQuantityGraph)
                    )

                    if (newQuantityGraph == 1) {
                        orderSummaryGraph.insertOne(
                            OrderItemBarGraph(
                                createdDate = orderDate,
                                pincode = order.society_pincode,
                                sellerId = sellerId,
                                quantity = newQuantityGraph
                            )
                        )
                    } else {
                        orderSummaryGraph.updateOne(filter, summaryUpdate)
                    }

                    // Fetch FCM tokens
                    val fcmTokens = adminAcessCollection.find(
                        and(
                            adminAcess::society_pincode eq order.society_pincode,
                            adminAcess::sellerId eq sellerId
                        )
                    ).toList()
                    fcmAdded.addAll(fcmTokens.map { it.fcm_token.toString() })
                }
            }

            order.fcm_tokenSeller = fcmAdded
            var totalItemCount = 0


            orders.forEach { orderItem ->
                order.orderList = orders
                order.sellerId = sellerId
                order.orderId = "OD${System.currentTimeMillis()}"
                combineOrderId += "${order.orderId}\n"

                val totalOrderValue = orders.sumOf { it.productprice?.toInt() ?: 0 }
                totalItemCount += orders.size

                order.totalOrderValue = totalOrderValue.toString()
                orderdetails.insertOne(order)
            }

            // Update user data
            val user = userCollection.find(Users::phone eq order.mobilenumber).first()
            val newQuantity = user?.order?.first()?.plus(totalItemCount) ?: totalItemCount
            val update = Document(
                "\$set", Document("email", user?.email)
                    .append("name", user?.name)
                    .append("phone", user?.phone)
                    .append("cancel", user?.cancel)
                    .append("deliver", user?.deliver)
                    .append("order", newQuantity)
                    .append("profileImage", user?.profileImage)
                    .append("fcmtoken", user?.fcmtoken)
                    .append("changetime", System.currentTimeMillis().toDouble())
            )
            userCollection.updateOne(Document("phone", user?.phone), update)
        }

        orderTemp.fcm_tokenSeller = fcmAdded
        orderTemp.orderId = combineOrderId

        return orderTemp
    }


    suspend fun getAllOrder(
        status: String,
        mobileNumber: String? = null,
        pincode: String? = null,
        sellerId: String? = null
    ): List<OrderItem> {

        return orderdetails.find(
            OrderItem::orderStatus eq status.replace("\"", ""),
            if (sellerId?.isNotEmpty() == true) OrderItem::sellerId eq sellerId.replace("\"", "") else null,
            if (pincode?.isNotEmpty() == true) OrderItem::society_pincode eq pincode.replace("\"", "") else null
        ).toList()

    }

    suspend fun getAllOrder1(
        status: String,
        mobileNumber: String? = null,
        society_pincode: String? = null,
        sellerId: String? = null
    ): List<OrderItem> {

        return orderdetails.find(
            OrderItem::orderStatus eq status.replace("\"", ""),
            if (mobileNumber?.isNotEmpty() == true) OrderItem::mobilenumber eq mobileNumber.replace("\"", "") else null,
            if (society_pincode?.isNotEmpty() == true) OrderItem::society_pincode eq society_pincode.replace(
                "\"",
                ""
            ) else null
        ).toList()

    }

    suspend fun getAllOrderBasedOnDateRange(
        sellerId: String,
        startDate: String,
        endDate: String? = null
    ): MutableList<OrderQtyDates> {
        val ls: MutableList<OrderQtyDates> = mutableListOf()

        val sdf = SimpleDateFormat("dd/MM/yyyy")
        var startDateTrimmed = startDate.trim()
        var endDateTrimmed = (endDate ?: startDate).trim()

        var currentDate = sdf.parse("$startDateTrimmed")
        val endDateParsed = sdf.parse("$endDateTrimmed")

        while (currentDate <= endDateParsed) {
            val formattedDate = sdf.format(currentDate)
            val dayName = SimpleDateFormat("EEE").format(currentDate.time)
            println("Formatted Date: $formattedDate") // Print for debugging

            val orderCount = orderSummaryGraph.find(
                OrderItemBarGraph::createdDate eq formattedDate,
                OrderItemBarGraph::sellerId eq sellerId
            ).toList()
            if (orderCount.isNotEmpty())
                ls.add(OrderQtyDates(dayName, orderCount.get(0).quantity))
            else
                ls.add(OrderQtyDates(dayName, 0))

            val calendar = Calendar.getInstance()
            calendar.time = currentDate
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            currentDate = calendar.time
        }

        return ls
    }

    suspend fun getAllOrderPagination(
        skip: Int?,
        limit: Int?,
        society_pincode: String,
        sellerId: String
    ): List<OrderItem> =
        orderdetails.find(
            OrderItem::society_pincode eq society_pincode.replace("\"", ""),
            OrderItem::sellerId eq sellerId.replace("\"", "")
        ).skip(skip ?: 0).limit(limit ?: 0).toList()

    suspend fun getAllUsers(): List<Users> = userCollection.find().toList()


    //get home products
    suspend fun getSearchAllProducts(
        string: Regex,
        society_pincode: String?,
        sellerId: String? = null
    ): List<HomeProducts> =
        home_collections.find(
            HomeProducts::productName regex string,
            HomeProducts::society_pincode eq society_pincode,
            if (sellerId?.isNotEmpty() == true) HomeProducts::sellerId eq sellerId.replace("\"", "") else null
        ).toList()

    suspend fun getHomeAllProducts(
        offset: Int? = 0,
        limit: Int? = 0,
        category: String? = "",
        society_pincode: String?,
        sellerId: String? = null
    ): List<HomeProducts> =
        home_collections.find(
            if (category != null) HomeProducts::item_subcategory_name eq category.replace(
                "\"",
                ""
            ) else null,
            HomeProducts::society_pincode eq society_pincode?.replace("\"", ""),
            if (sellerId?.isNotEmpty() == true) HomeProducts::sellerId eq sellerId.replace("\"", "") else null
        ).skip(offset ?: 0).limit(limit ?: 0).toList()

    suspend fun GetPendingProductById(productId: String): HomeProducts? =
        home_collections.find(HomeProducts::productId eq productId).first()

    suspend fun getLastProductId(): String? =
        home_collections.find().toList()[home_collections.find().toList().size - 1].productId

    suspend fun getItemCount(): Int = home_collections.find().toList().size - 1
    suspend fun getExclusiveProductBasedId(productId: String): HomeProducts? =
        home_collections.find(HomeProducts::productId eq productId).first()

    suspend fun getBestProductBasedId(productId: String): HomeProducts? =
        home_collections.find(HomeProducts::productId eq productId).first()

    suspend fun getAllCoupons(updateAddressItem: String?): List<AddCouponRequest>? =
        allCoupons.find(AddCouponRequest::society_pincode eq updateAddressItem).toList()


    suspend fun getRelatedSearch(pincode: String): List<HomeProducts> =
        home_collections.find(HomeProducts::society_pincode eq pincode).toList()


    suspend fun getAllExclusiveCollection(): List<ExclusiveOffers> =
        exclusiveCollection.find().toList()

    suspend fun getHomeAllProducts1(pincode: String, sellerId: String? = null): List<HomeProducts> =
        home_collections.find(
            HomeProducts::society_pincode eq pincode,
            if (sellerId?.isNotEmpty() == true) HomeProducts::sellerId eq sellerId.replace("\"", "") else null
        ).toList()

    suspend fun getAlUsers(): List<Users> = userCollection.find().toList()

    suspend fun getAllAdmins(): List<adminAcess> = adminAcessCollection.find().toList()

    suspend fun setOrderStatus(req: OrderItem, sendEmail: (String) -> Unit): Long {
        var result = 0L

        if (req.orderStatus == "Cancelled") {
            val obj = userCollection.find(Users::phone eq req.mobilenumber).first()
            val objDatabase = orderdetails.find(OrderItem::orderId eq req.orderId).first()
            if (objDatabase?.isStatusAlreadyUpdated == true) {
                return -1//means already updated
            }
            val newQuantity = obj?.cancel?.first()?.plus(1) ?: 1
            val updateData = Document(
                "\$set",
                Document("email", obj?.email)

                    .append("name", obj?.name)
                    .append("phone", obj?.phone)
                    .append("cancel", newQuantity)
                    .append("deliver", obj?.deliver)
                    .append("order", obj?.order)
                    .append("isStatusAlreadyUpdated", true)
                    .append("profileImage", obj?.profileImage)
                    .append("fcmtoken", obj?.fcmtoken)
                    .append("changetime", System.currentTimeMillis().toDouble())
            )
            result = userCollection.updateOne(Document("phone", obj?.phone), updateData).modifiedCount
        } else if (req.orderStatus == "Delivered") {
            val obj = userCollection.find(Users::phone eq req.mobilenumber).first()
            val newQuantity = obj?.deliver?.first()?.plus(1) ?: 1
            val updateData = Document(
                "\$set",
                Document("email", obj?.email)

                    .append("name", obj?.name)
                    .append("phone", obj?.phone)
                    .append("cancel", obj?.cancel)
                    .append("deliver", newQuantity)
                    .append("order", obj?.order)
                    .append("isStatusAlreadyUpdated", true)
                    .append("profileImage", obj?.profileImage)
                    .append("fcmtoken", obj?.fcmtoken)
                    .append("changetime", System.currentTimeMillis().toDouble())
            )
            result = userCollection.updateOne(Document("phone", obj?.phone), updateData).modifiedCount
        } else {

            val update = Document(
                "\$set",
                Document("orderId", req.orderId)

                    .append("totalOrderValue", req.totalOrderValue)

                    .append("orderList", req.orderList)
                    .append("isStatusAlreadyUpdated", true)
                    .append("address", req.address)
                    .append("createdDate", req.createdDate)
                    .append("mobilenumber", req.mobilenumber)
                    .append("paymentmode", req.paymentmode)
                    .append("pincode", req.society_pincode)
                    .append("changeTime", req.changeTime)
                    .append("orderStatus", req.orderStatus)

            )


            result = orderdetails.updateOne(Document("orderId", req.orderId), update).modifiedCount
//    if(result.modifiedCount>0){
//        val obj= userCollection.find(( Users::phone eq req.mobilenumber.replace("+",""))).first()
//        //  sendNotification(obj?.fcmtoken?:"","order ${req.orderStatus} ","check your orders")
//        //sendEmail(obj?.email?:"")
//    }
        }
        return result
    }


    suspend fun getProductSubItems(productId: String, pincode: String?): List<HomeProducts?> =
        home_collections.find(
            HomeProducts::item_subcategory_name eq productId,
            HomeProducts::society_pincode eq pincode
        ).toList()

    suspend fun getProductAllSubItems(productId: String, sellerId: String?): List<HomeProducts?> =
        home_collections.find(HomeProducts::item_subcategory_name eq productId, HomeProducts::sellerId eq sellerId)
            .toList()


    suspend fun getUserByPhone(phone: String): Users? =
        userCollection.find(Users::phone eq phone.replace("\"", "")).first()

    suspend fun checkNumberExist(phone: String): List<Users> =
        userCollection.find(Users::phone eq phone).toList()

    suspend fun deleteUserById(userId: String): Boolean =
        userCollection.deleteOne(Users::userId eq userId).wasAcknowledged()

    suspend fun userCheck(email: String, passord: String): adminAcess =
        adminAcessCollection.find((adminAcess::password eq "1234567")).first()!!

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

    ): Long {
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
        if (categoryName.split("__")[1] == "bannercategory1")
            adminItemCategory.deleteOne(BannerCategory::bannercategory1 eq categoryName).wasAcknowledged()
        else
            adminItemCategory.deleteOne(BannerCategory::bannercategory2 eq categoryName).wasAcknowledged()

    //    suspend fun deleteProductsBasedCategory(categoryName: String): Boolean =
//        home_collections.deleteMany(HomeProducts:: eq categoryName).wasAcknowledged()
    suspend fun deleteCoupon(couponName: String): Boolean =
        allCoupons.deleteOne(AddCouponRequest::couponCode eq couponName).wasAcknowledged()


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
                .append("pincode", req.society_pincode)
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
                    .append("pincode", req.society_pincode)
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

        val obj = userCollection.find((Users::phone eq mobile)).first()
        val update = Document(
            "\$set",
            Document("email", obj?.email)

                .append("name", obj?.name)

                .append("phone", obj?.phone)
                .append("cancel", obj?.cancel)
                .append("deliver", obj?.deliver)
                .append("order", obj?.order)
                .append("profileImage", obj?.profileImage)
                .append("fcmtoken", token)
                .append("changetime", System.currentTimeMillis().toDouble())


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

    suspend fun getAllProductSubItems(productId: String, pincode: String?): List<HomeProducts?> =
        home_collections.find(
            HomeProducts::item_subcategory_name eq productId,
            HomeProducts::society_pincode eq pincode
        ).toList()


}