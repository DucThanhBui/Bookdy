package com.example.bookdy.data.model

import androidx.annotation.ColorInt
import androidx.room.*
import org.json.JSONObject
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.util.Url
import org.readium.r2.shared.util.mediatype.MediaType

/**
 * @param id Primary key, auto-incremented
 * @param style Look and feel of this annotation (highlight, underline)
 * @param title Provides additional context about the annotation
 * @param tint Color associated with the annotation
 * @param bookId Foreign key to the book
 * @param href References a resource within a publication
 * @param type References the media type of a resource within a publication
 * @param totalProgression Overall progression in the publication
 * @param locations Locator locations object
 * @param text Locator text object
 * @param annotation User-provided note attached to the annotation
 */
@Entity(
    tableName = "highlights",
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = [Book.IDENTIFIER],
            childColumns = [Highlight.BOOK_IDF],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = [Highlight.BOOK_IDF])]
)
data class Highlight(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ID)
    var id: Long = 0,
    @ColumnInfo(name = BOOK_IDF)
    val bookIdf: String,
    @ColumnInfo(name = TINT, defaultValue = "0")
    @ColorInt
    var tint: Int,
    @ColumnInfo(name = HREF)
    var href: String,
    @ColumnInfo(name = TYPE)
    var type: String,
    @ColumnInfo(name = TITLE, defaultValue = "NULL")
    var title: String? = null,
    @ColumnInfo(name = TOTAL_PROGRESSION, defaultValue = "0")
    var totalProgression: Double = 0.0,
    @ColumnInfo(name = LOCATIONS, defaultValue = "{}")
    var locations: Locator.Locations = Locator.Locations(),
    @ColumnInfo(name = TEXT, defaultValue = "{}")
    var text: Locator.Text = Locator.Text(),
    @ColumnInfo(name = ANNOTATION, defaultValue = "")
    var annotation: String = ""
) {

    constructor(
        bookIdf: String,
        @ColorInt tint: Int,
        locator: Locator,
        annotation: String
    ) :
        this(
            bookIdf = bookIdf,
            tint = tint,
            href = locator.href.toString(),
            type = locator.mediaType.toString(),
            title = locator.title,
            totalProgression = locator.locations.totalProgression ?: 0.0,
            locations = locator.locations,
            text = locator.text,
            annotation = annotation
        )

    val locator: Locator get() = Locator(
        href = Url(href)!!,
        mediaType = MediaType(type) ?: MediaType.BINARY,
        title = title,
        locations = locations,
        text = text
    )

    companion object {
        const val TABLE_NAME = "HIGHLIGHTS"
        const val ID = "ID"
        const val BOOK_IDF = "BOOK_IDF"
        const val TINT = "TINT"
        const val HREF = "HREF"
        const val TYPE = "TYPE"
        const val TITLE = "TITLE"
        const val TOTAL_PROGRESSION = "TOTAL_PROGRESSION"
        const val LOCATIONS = "LOCATIONS"
        const val TEXT = "TEXT"
        const val ANNOTATION = "ANNOTATION"
    }
}

class HighlightConverters {
    @TypeConverter
    fun textFromString(value: String?): Locator.Text = Locator.Text.fromJSON(
        value?.let { JSONObject(it) }
    )

    @TypeConverter
    fun textToString(text: Locator.Text): String = text.toJSON().toString()

    @TypeConverter
    fun locationsFromString(value: String?): Locator.Locations = Locator.Locations.fromJSON(
        value?.let { JSONObject(it) }
    )

    @TypeConverter
    fun locationsToString(text: Locator.Locations): String = text.toJSON().toString()
}
