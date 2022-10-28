package com.example.src.data

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId


data class Users(
    @BsonId
    val userId: String? = ObjectId().toString(),
    val email: String? = null,
    val name: String?= null,
    val phone:String?=null,
    val profileImage:String?=null
)

