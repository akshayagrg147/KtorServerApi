package com.example.src.data
     data class HomeProducts(val productName:String="", val price:String="", val quantity:String="", val ProductImage1: String?=null,val ProductImage2: String?=null,val ProductImage3: String?=null,val productId:String="",val ProductDescription:String?=null,val actual_price:String?=null,val DashboardDisplay:Boolean?=false,val category:String?=null,val rating:List<subrating> = emptyList<subrating>(),val itemCategoryId:String?=null,val categoryType:Int=0)
data class exclusiveOffers(var productName:String="", var price:String="", var quantity:String="", var ProductImage1: String?=null, var ProductImage2: String?=null, var ProductImage3: String?=null, var productId:String="", var ProductDescription:String?=null, var actual_price:String?=null, var DashboardDisplay:Boolean?=false, var category:String?=null, var rating:List<subrating> = emptyList<subrating>(), var itemCategoryId:String?=null, var categoryType:Int=0)
data class bestSelling(var productName:String="", var price:String="", var quantity:String="", var ProductImage1: String?=null, var ProductImage2: String?=null, var ProductImage3: String?=null, var productId:String?, var ProductDescription:String?=null, var actual_price:String?=null, var DashboardDisplay:Boolean?=false, var category:String?=null, var rating:List<subrating> = emptyList<subrating>(), var itemCategoryId:String?=null, var categoryType:Int=0)

data class itemsCollections(var productName:String="", var price:String="", var quantity:String="", var ProductImage1: String?=null, var ProductImage2: String?=null, var ProductImage3: String?=null, var productId:String?="", var ProductDescription:String?=null, var actual_price:String?=null, var DashboardDisplay:Boolean?=false, var category:String?=null, var rating:List<subrating> = emptyList<subrating>(), var itemCategoryId:String?=null, var categoryType:Int=0)

data class SearchByProductId (val ProductId:String?=null)
data class CityAailibilty (val city:String?=null)

data class RelatedSerachByPriceAndCategory (val Price:String?=null,val category:String?=null)
data class UserRequest(val phone:String?=null)
data class PaginationPassing(val offset:Int?=0,val limit:Int?=0)
data class subrating (val remark:String?=null,val rating:String?=null,val name:String?=null,val customerId:String?=null)

data class orderitem(val orderList:ArrayList<Orders>, val totalOrderValue:String?=null, val paymentmode:String?=null, val address:String?=null, val mobilenumber:String,var createdDate:String,var orderId:String)

data class Orders (
     val productId:String?=null, val productName:String?=null, val quantity:String?=null, val productprice:String?=null)