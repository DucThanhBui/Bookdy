package com.example.bookdy.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.bookdy.data.model.Bookmark
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.mediatype.MediaType

@Entity(tableName = Book.TABLE_NAME)
data class Book(
    @PrimaryKey
    @ColumnInfo(name = IDENTIFIER)
    val identifier: String,
    @ColumnInfo(name = Bookmark.CREATION_DATE, defaultValue = "CURRENT_TIMESTAMP")
    val creation: String? = null,
    @ColumnInfo(name = HREF)
    val href: String,
    @ColumnInfo(name = TITLE)
    val title: String?,
    @ColumnInfo(name = AUTHOR)
    val author: String? = null,
    @ColumnInfo(name = PROGRESSION)
    val progression: String? = null,
    @ColumnInfo(name = MEDIA_TYPE)
    val rawMediaType: String,
    @ColumnInfo(name = COVER)
    val cover: String
) {

    constructor(
        creation: String? = null,
        href: String,
        title: String?,
        author: String? = null,
        identifier: String,
        progression: String? = null,
        mediaType: MediaType,
        cover: String
    ) : this(
        creation = creation,
        href = href,
        title = title,
        author = author,
        identifier = identifier,
        progression = progression,
        rawMediaType = mediaType.toString(),
        cover = cover
    )

    val url: AbsoluteUrl get() = AbsoluteUrl(href)!!

    val mediaType: MediaType get() =
        MediaType(rawMediaType)!!

    companion object {

        const val TABLE_NAME = "books"
        const val CREATION_DATE = "creation_date"
        const val HREF = "href"
        const val TITLE = "title"
        const val AUTHOR = "author"
        const val IDENTIFIER = "identifier"
        const val PROGRESSION = "progression"
        const val MEDIA_TYPE = "media_type"
        const val COVER = "cover"
    }
}
