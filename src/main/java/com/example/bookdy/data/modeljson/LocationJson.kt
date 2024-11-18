package com.example.bookdy.data.modeljson

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
class LocationJson (
    @SerializedName("fragments")
    val fragments: List<String> = emptyList(),
    @SerializedName("progression")
    val progression: String?,  //-> double
    @SerializedName("position")
    val position: Int?,
    @SerializedName("totalProgression")
    val totalProgression: String?, //->double
    @SerializedName("fragments")
    val otherLocations: String
): Parcelable