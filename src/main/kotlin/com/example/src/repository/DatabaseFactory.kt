package com.example.src.repository




import com.example.src.data.*

import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.regex
import java.net.URLEncoder

class DatabaseFactory {



    private val username = URLEncoder.encode("akshaygarg147", "UTF-8")

    private val password = URLEncoder.encode("Akshaygarg147@", "UTF-8")


    private val url = "mongodb+srv://$username:$password@cluster0.qkvbexc.mongodb.net/"


    private val client = KMongo.createClient().coroutine
    private val database = client.getDatabase("test")
    val userCollection: CoroutineCollection<Users> = database.getCollection()
    val orderdetails: CoroutineCollection<orderitem> = database.getCollection()
    val productcollection: CoroutineCollection<HomeProducts> = database.getCollection()
    val home_collections: CoroutineCollection<HomeProducts> = database.getCollection()
    val allProductCollection:CoroutineCollection<HomeProducts> =database.getCollection()
    val addExclusiveCollection:CoroutineCollection<exclusiveOffers> =database.getCollection()
    val addBestSellngCollection:CoroutineCollection<bestSelling> =database.getCollection()
    val additemsCollections:CoroutineCollection<itemsCollections> =database.getCollection()

    suspend fun addadditemsCollectionsAdminDashboard(request: itemsCollections):itemsCollections{
        additemsCollections.insertOne(request)
        return request
    }
suspend fun addProductAdminDashboard(request: HomeProducts):HomeProducts{
    allProductCollection.insertOne(request)
    return request
}

    suspend fun addExclusiveAdminDashboard(request: exclusiveOffers):exclusiveOffers{
        addExclusiveCollection.insertOne(request)
        return request
    }

    suspend fun addBestSellingAdminDashboard(request: bestSelling):bestSelling{
        addBestSellngCollection.insertOne(request)
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


    suspend fun homeproducts(products: HomeProducts): HomeProducts {
        home_collections.insertOne(products)
        return products
    }
    suspend fun getAllOrder():List<orderitem> = orderdetails.find().toList()

    suspend fun getAllOrderPagination(offset: Int?, limit: Int?):List<orderitem> = orderdetails.find().skip(offset?:0).limit(limit?:0).toList()
    suspend fun getAllUsers():List<Users> = userCollection.find().toList()

   //get home products
   suspend fun getSearchAllProducts( string:Regex,):List<HomeProducts> = home_collections.find(HomeProducts::productName regex  string ).toList()

    suspend fun getHomeAllProducts( offset:Int?=0,limit:Int?=0,category: String?=""):List<HomeProducts> = home_collections.find(HomeProducts::category eq category).skip(offset?:0).limit(limit?:0).toList()

    suspend fun GetPendingProductById(productId:String):HomeProducts?= home_collections.find(HomeProducts::productId  eq productId).first()
    suspend fun getLastProductId():String?= home_collections.find().toList()[home_collections.find().toList().size-1].productId
    suspend fun getExclusiveProductBasedId(productId:String):HomeProducts?= home_collections.find(HomeProducts::productId eq productId).first()

    suspend fun getBestProductBasedId(productId:String):HomeProducts?= home_collections.find(HomeProducts::productId eq productId).first()


    suspend fun getRelatedSearch(price: String):List<HomeProducts> = home_collections.find().toList()


    suspend fun getaddExclusiveCollection():List<exclusiveOffers> =
        addExclusiveCollection.find().toList()
    suspend fun getHomeAllProducts():List<HomeProducts> =
        home_collections.find().toList()

    suspend fun getAlUsers():List<Users> = userCollection.find().toList()
    suspend fun getProductSubItems(productId:String):List<HomeProducts?> = productcollection.find(HomeProducts::itemCategoryId eq productId).toList()

    suspend fun getUserByPhone(phone:String):Users? = userCollection.find(Users::phone eq phone).first()
    suspend fun CheckNumberExist(phone:String):Boolean= userCollection.find(Users::phone eq phone).toList().isNotEmpty()
    suspend fun deleteUserById(userId: String):Boolean = userCollection.deleteOne(Users::userId eq userId).wasAcknowledged()
    suspend fun deleteProductById(productId: String):Boolean = allProductCollection.deleteOne(HomeProducts::productId eq productId).wasAcknowledged()
}