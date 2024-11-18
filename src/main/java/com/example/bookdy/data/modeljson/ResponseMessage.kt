package com.example.bookdy.data.modeljson

import com.google.gson.annotations.SerializedName

class ResponseMessage(
    @SerializedName("status")
    val status: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("token")
    val token: String? = null
) {
}