package com.example.src.modal

data class ApiResponse(
    val message: String,
    val status: Boolean,
    val statusCode: Int,
    )
data class RequestLoginBody(val email: String,val password: String)