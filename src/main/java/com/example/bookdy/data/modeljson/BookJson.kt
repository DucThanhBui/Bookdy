package com.example.bookdy.data.modeljson

import android.annotation.SuppressLint
import android.os.Parcelable
import android.util.Log
import com.example.bookdy.data.model.Highlight
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class BookJson (
    @SerializedName("creation")
    val creation: String,
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
    val highlights: List<HighlightJson>,
    @SerializedName("pathOnServer")
    var pathOnServer: String = "",
    @SerializedName("cover")
    var cover: String = "",
    @SerializedName("username")
    var username: String = ""
): Parcelable {
    @SuppressLint("LogNotTimber")
    fun toJsonString(): String {
        val gson = Gson()
        Log.d(TAG, "maybe crashing")
        var bookmarkJson = ""
        bookmarks.forEach {
            val tmp = gson.toJson(it)
            Log.d(TAG, "bookmark tmp is $tmp")
            bookmarkJson += tmp
        }
        bookmarkJson += ""
        Log.d(TAG, "bookmark json is $bookmarkJson")

        var hlJson = "["
        highlights.forEach {
            hlJson += gson.toJson(it)
            Log.d(TAG, "hl tmp is ${gson.toJson(it)}")
        }
        hlJson += ""
        Log.d(TAG, "hl json is $hlJson")

        return "{\"creation\":\"$creation\"," +
            "\"filename\":\"$filename\"," +
            "\"identifier\":\"$identifier\"," +
            "\"author\":\"$author\"," +
            "\"progression\":\"$progression\"," +
            "\"rawMediaType\":\"$rawMediaType\"," +
            "\"bookmarks\":[$bookmarkJson],\"highlights\":[$hlJson]}"

    }
    companion object {
        const val TAG = "Readiumxxx"
    }
}