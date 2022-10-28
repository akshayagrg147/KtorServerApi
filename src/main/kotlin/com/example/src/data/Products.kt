package com.example.src.data

data class ExclusiveOffers (val productName:String, val price:String, val quantity:String, val ProductImage1: String?=null,val ProductImage2: String?=null,val ProductImage3: String?=null,val productId:String?,val ProductDescription:String?=null,val actual_price:String?=null,val rating:List<subrating> = emptyList<subrating>())
data class BestSelling (val productName:String, val price:String, val quantity:String, val ProductImage1: String?=null,val ProductImage2: String?=null,val ProductImage3: String?=null,val productId:String?,val ProductDescription:String?=null,val actual_price:String?=null,val rating:List<subrating> = emptyList<subrating>())
data class HomeProducts(val productName:String, val price:String, val quantity:String, val ProductImage1: String?=null,val ProductImage2: String?=null,val ProductImage3: String?=null,val productId:String?,val ProductDescription:String?=null,val actual_price:String?=null,val rating:List<subrating> = emptyList<subrating>())
data class ItemsCollections(val productName:String, val price:String, val quantity:String, val ProductImage1: String?=null,val ProductImage2: String?=null,val ProductImage3: String?=null,val productId:String?,val ProductDescription:String?=null,val actual_price:String?=null,val itemCategoryId:String ?=null)


data class SearchByProductId (val ProductId:String?=null)
data class UserRequest(val phone:String?=null)
data class subrating (val remark:String?=null,val rating:String?=null,val name:String?=null,val customerId:String?=null)

data class orderitem(val orderList:ArrayList<Orders>, val totalOrderValue:String?=null, val paymentmode:String?=null, val address:String?=null, val mobilenumber:String,var createdDate:String,var orderId:String)

data class Orders (
     val productId:String?=null, val product_name:String?=null, val quantity:String?=null, val productprice:String?=null)