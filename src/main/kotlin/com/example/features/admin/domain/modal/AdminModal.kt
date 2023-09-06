package com.example.src.modal

data class ApiResponse(
    val message: String,
    val status: Boolean,
    val statusCode: Int,
    )
data class RequestLoginBody(val email: String,val password: String)
data class RequestCoupon(val couponName: String)
data class OrderStatusRequest(val status: String)
data class AddCouponRequest(
    var couponTitle:String, val couponCode:String,  val discountPercentage:String,
    val discountedAmount:String, val minimumPurchase:String, val startDate:String, val expireDate:String)