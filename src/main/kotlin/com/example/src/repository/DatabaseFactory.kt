package com.example.src.repository




import com.example.src.data.*

import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.coroutine.insertOne
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo

class DatabaseFactory {

    private val client = KMongo.createClient().coroutine
    private val database = client.getDatabase("test")
    val userCollection: CoroutineCollection<Users> = database.getCollection()
    val exclusive_collection: CoroutineCollection<ExclusiveOffers> = database.getCollection()
    val BestSelling_collection: CoroutineCollection<BestSelling> = database.getCollection()
    val home_collections: CoroutineCollection<HomeProducts> = database.getCollection()


    suspend fun addUser(users: Users): Users {
        userCollection.insertOne(users)
        return users
    }

    suspend fun exclusiveoffers(products: ExclusiveOffers): ExclusiveOffers {
        exclusive_collection.insertOne(products)
        return products
    }
    suspend fun bestselling(products: BestSelling): BestSelling {
        BestSelling_collection.insertOne(products)
        return products
    }
    suspend fun homeproducts(products: HomeProducts): HomeProducts {
        home_collections.insertOne(products)
        return products
    }

    suspend fun getAllUsers():List<Users> = userCollection.find().toList()

   //get home products
    suspend fun getHomeAllProducts():List<HomeProducts> = home_collections.find().toList()
    suspend fun GetPendingProductById(productId:String):HomeProducts?= home_collections.find(HomeProducts::productId  eq productId).first()
    suspend fun getExclusiveProductBasedId(productId:String):ExclusiveOffers?= exclusive_collection.find(ExclusiveOffers::productId eq productId).first()

    suspend fun getBestProductBasedId(productId:String):BestSelling?= BestSelling_collection.find(BestSelling::productId eq productId).first()



    suspend fun getBestAllProducts():List<BestSelling> = BestSelling_collection.find().toList()
    suspend fun getExcusiveAllProducts():List<ExclusiveOffers> = exclusive_collection.find().toList()

    suspend fun getUserByPhone(phone:String):Users? = userCollection.find(Users::phone eq phone).first()
    suspend fun CheckNumberExist(phone:String):Boolean= userCollection.find(Users::phone eq phone).toList().isNotEmpty()
    suspend fun deleteUserById(userId: String):Boolean = userCollection.deleteOne(Users::userId eq userId).wasAcknowledged()

}