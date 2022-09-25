package com.example.src.data

data class ExclusiveOffers (val productName:String, val price:String, val quantity:String, val ProductImage1: String?=null,val ProductImage2: String?=null,val ProductImage3: String?=null,val productId:String?,val ProductDescription:String?=null,val actual_price:String?=null)
data class BestSelling (val productName:String, val price:String, val quantity:String, val ProductImage1: String?=null,val ProductImage2: String?=null,val ProductImage3: String?=null,val productId:String?,val ProductDescription:String?=null,val actual_price:String?=null)
data class HomeProducts(val productName:String, val price:String, val quantity:String, val ProductImage1: String?=null,val ProductImage2: String?=null,val ProductImage3: String?=null,val productId:String?,val ProductDescription:String?=null,val actual_price:String?=null)

data class SearchByProductId (val ProductId:String)