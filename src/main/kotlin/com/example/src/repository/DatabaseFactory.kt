package com.example.src.repository

import com.example.features.customer.domain.modal.Users
import com.example.src.modal.*
import org.bson.Document
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.coroutine.insertOne
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.regex
import java.net.URLEncoder

class DatabaseFactory {
    private val username = URLEncoder.encode("akshaygarg147", "UTF-8")
    private val password = URLEncoder.encode("Akshaygarg147@", "UTF-8")
    private val url = "mongodb+srv://$username:$password@cluster0.qkvbexc.mongodb.net/"
    private val client = KMongo.createClient().coroutine
    private val database = client.getDatabase("groceryMain")
    val userCollection: CoroutineCollection<Users> = database.getCollection()
    val orderdetails: CoroutineCollection<orderitem> = database.getCollection()
    val home_collections: CoroutineCollection<HomeProducts> = database.getCollection()
    val exclusiveCollection: CoroutineCollection<exclusiveOffers> = database.getCollection()
    val bestSellngCollection: CoroutineCollection<bestSelling> = database.getCollection()
    val adminItemCategory: CoroutineCollection<ProductCategory> = database.getCollection()
    val addCouponRequest:CoroutineCollection<AddCouponRequest> = database.getCollection()
    val allCoupons:CoroutineCollection<AddCouponRequest> = database.getCollection()

    suspend fun addCategory(request: ProductCategory): ProductCategory {
        adminItemCategory.insertOne(request)
        return request
    }
    suspend fun addCoupon(request:AddCouponRequest):AddCouponRequest{
        addCouponRequest.insertOne(request)
        return request
    }



    suspend fun getProductCategory(): List<ProductCategory> {
        return adminItemCategory.find().toList()
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
        orderdetails.insertOne(order)
        return order
    }


    suspend fun getAllOrder(status:String): List<orderitem> = orderdetails.find(orderitem::orderStatus eq status.replace("\"", "")).toList()

    suspend fun getAllOrderPagination(offset: Int?, limit: Int?): List<orderitem> =
        orderdetails.find().skip(offset ?: 0).limit(limit ?: 0).toList()

    suspend fun getAllUsers(): List<Users> = userCollection.find().toList()

    //get home products
    suspend fun getSearchAllProducts(string: Regex): List<HomeProducts> =
        home_collections.find(HomeProducts::productName regex string).toList()

    suspend fun getHomeAllProducts(offset: Int? = 0, limit: Int? = 0, category: String? = ""): List<HomeProducts> =
        home_collections.find(HomeProducts::item_category_name eq category).skip(offset ?: 0).limit(limit ?: 0).toList()

    suspend fun GetPendingProductById(productId: String): HomeProducts? =
        home_collections.find(HomeProducts::productId eq productId).first()

    suspend fun getLastProductId(): String? =
        home_collections.find().toList()[home_collections.find().toList().size - 1].productId

    suspend fun getItemCount(): Int = home_collections.find().toList().size - 1
    suspend fun getExclusiveProductBasedId(productId: String): HomeProducts? =
        home_collections.find(HomeProducts::productId eq productId).first()

    suspend fun getBestProductBasedId(productId: String): HomeProducts? =
        home_collections.find(HomeProducts::productId eq productId).first()

    suspend fun getAllCoupons(): List<AddCouponRequest>? =
        allCoupons.find().toList()


    suspend fun getRelatedSearch(price: String): List<HomeProducts> = home_collections.find().toList()


    suspend fun getAllExclusiveCollection(): List<exclusiveOffers> =
        exclusiveCollection.find().toList()

    suspend fun getHomeAllProducts(): List<HomeProducts> =
        home_collections.find().toList()

    suspend fun getAlUsers(): List<Users> = userCollection.find().toList()

    suspend fun setOrderStatus(req: orderitem): Long {
        val update = Document(
            "\$set",
            Document("orderId", req.orderId)

                .append("totalOrderValue", req.totalOrderValue)

                .append("orderList", req.orderList)
                .append("address", req.address)
                .append("createdDate", req.createdDate)
                .append("mobilenumber", req.mobilenumber)
                .append("paymentmode", req.paymentmode)
                .append("changeTime", req.changeTime)
                .append("orderStatus", req.orderStatus)

        )

        val result = orderdetails.updateOne(Document("orderId", req.orderId), update)
        return result.modifiedCount
    }
    suspend fun getProductSubItems(productId: String): List<HomeProducts?> =
        home_collections.find(HomeProducts::item_subcategory_name eq productId).toList()

    suspend fun getUserByPhone(phone: String): Users? = userCollection.find(Users::phone eq phone).first()
    suspend fun checkNumberExist(phone: String): Boolean =
        userCollection.find(Users::phone eq phone).toList().isNotEmpty()

    suspend fun deleteUserById(userId: String): Boolean =
        userCollection.deleteOne(Users::userId eq userId).wasAcknowledged()

    suspend fun deleteProductById(productId: String): Boolean =
        home_collections.deleteOne(HomeProducts::productId eq productId).wasAcknowledged()

    suspend fun deleteCategory(categoryName: String): Boolean =
        adminItemCategory.deleteOne(ProductCategory::category eq categoryName).wasAcknowledged()

//    suspend fun deleteProductsBasedCategory(categoryName: String): Boolean =
//        home_collections.deleteMany(HomeProducts:: eq categoryName).wasAcknowledged()
    suspend fun deleteCoupon(couponName: String): Boolean =
        allCoupons.deleteOne(ProductCategory::category eq couponName).wasAcknowledged()



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
                .append("productExclusiveSelling", req.productExclusiveSelling)
        )

        val result = home_collections.updateOne(Document("productId", req.productId), update)
        return result.modifiedCount
    }
}