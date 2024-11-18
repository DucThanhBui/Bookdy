package com.example.bookdy.data.modeljson

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
class HighlightJson(
    @SerializedName("creation")
    val creation: String?,   //long
    @SerializedName("bookId")
    val bookId: String,  //bookIdef
    @SerializedName("style")
    val style: String,   //long
    @SerializedName("tint")
    val tint: Int,
    @SerializedName("href")
    val href: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("title")
    val title: String?,
    @SerializedName("totalProgression")
    val totalProgression: String, //to double
    @SerializedName("annotation")
    val annotation: String,
    @SerializedName("text")
    val text: TextJson,
    @SerializedName("locations")
    val locations: LocationJson,
) : Parcelable