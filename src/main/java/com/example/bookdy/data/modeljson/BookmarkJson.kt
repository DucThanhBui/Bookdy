package com.example.bookdy.data.modeljson

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class BookmarkJson(
    @SerializedName("creation")
    val creation: String?,   //long
    @SerializedName("resourceIndex")
    val resourceIndex: String,   //long
    @SerializedName("bookId")
    val bookId: String,  //bookIdef -> bookID long
    @SerializedName("resourceHref")
    val resourceHref: String,
    @SerializedName("resourceType")
    val resourceType: String,
    @SerializedName("resourceTitle")
    val resourceTitle: String,
    @SerializedName("location")
    val location: String = "{}",
    @SerializedName("locatorText")
    val locatorText: String,
) : Parcelable