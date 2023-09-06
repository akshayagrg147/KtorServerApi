package com.example.utils

import com.example.src.modal.ApiResponse
import com.example.src.modal.CommonListResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*


suspend fun PipelineContext<Unit, ApplicationCall>.apiResponse(
    statusCodeApi:HttpStatusCode,
    statusCode: Int = 400,
    message: String = "Something Went Wrong",
    status:Boolean = false
) {


    call.respond(
        status = statusCodeApi,
        ApiResponse(status = status, statusCode = statusCode, message = message)
    )

}

suspend fun <T>PipelineContext<Unit, ApplicationCall>.apiListResponse(
    statusCodeApi:HttpStatusCode,
    statusCode: Int = 400,
    message: String = "Something Went Wrong",
    status:Boolean = false,
    ls:List<T>
) {


    call.respond(
        status = statusCodeApi,
        CommonListResponse(itemData = ls, statusCode = statusCode, message = message)
    )

}