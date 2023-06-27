package com.example.src.adminModalClass

data class addProductClass (val productId:String?=null, val productName:String?=null, val quantity:String?=null, val productprice:String?=null,val productDescription:String?=null,val productDeliveryInstruction:String,val pincode:String,val inStock:Boolean,val freeshipping:Boolean)
data class LoginResponseBody(
    val message: String,
    val status: Boolean,
    val statusCode: Int,

    )
data class RequestLoginBody(val email: String,val password: String)