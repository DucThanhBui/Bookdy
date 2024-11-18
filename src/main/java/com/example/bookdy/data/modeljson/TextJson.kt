package com.example.bookdy.data.modeljson

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
class TextJson(
    @SerializedName("before")
    val before: String? = null,
    @SerializedName("highlight")
    val highlight: String? = null,
    @SerializedName("after")
    val after: String? = null)
    : Parcelable
