package com.example.features.customer.domain.modal

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId


data class Users(
    @BsonId
    val userId: String? = ObjectId().toString(),
    val email: String? = null,
    val name: String? = null,
    val phone: String? = null,
    val profileImage: String? = null,
    var order: String? = "0",
    var cancel: String? = "0",
    var deliver: String? = "0",
    val fcmtoken: String? = null,
    val selectedOption: String? = null,
    val society_pincode: String? = null,
    val flatNumber: String? = null,
    val pincode: String? = null,
    val city: String? = null,

    )


