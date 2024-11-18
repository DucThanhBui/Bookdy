package com.example.bookdy.data.modeljson

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class BookJson (
    @SerializedName("creation")
    val creation: String,   //long
    @SerializedName("filename")
    val filename: String?,
    @SerializedName("identifier")
    val identifier: String,
    @SerializedName("author")
    val author: String?,
    @SerializedName("progression")
    val progression: String?,
    @SerializedName("rawMediaType")
    val rawMediaType: String,
    @SerializedName("bookmarks")
    val bookmarks: List<BookmarkJson>,
    @SerializedName("highlights")
    val highlights: List<HighlightJson>,   //long
): Parcelable