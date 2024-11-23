package com.example.bookdy.data.modeljson

import com.google.gson.annotations.SerializedName

class ResponseMessage(
    @SerializedName("status")
    val status: Int,
    @SerializedName("msg")
    val message: String,
    @SerializedName("access_token")
    val token: String? = null
) {
}