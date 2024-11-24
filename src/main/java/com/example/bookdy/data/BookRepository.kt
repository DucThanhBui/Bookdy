package com.example.bookdy.data

import androidx.annotation.ColorInt
import java.io.File
import kotlinx.coroutines.flow.Flow
import org.joda.time.DateTime
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.indexOfFirstWithHref
import org.readium.r2.shared.util.Url
import org.readium.r2.shared.util.mediatype.MediaType
import com.example.bookdy.data.db.BooksDao
import com.example.bookdy.data.model.Book
import com.example.bookdy.data.model.Bookmark
import com.example.bookdy.data.model.Highlight
import org.joda.time.format.DateTimeFormat

class BookRepository(
    private val booksDao: BooksDao
) {
    fun books(): Flow<List<Book>> = booksDao.getAllBooks()
    fun favoriteBooks(): Flow<List<Book>> = booksDao.getAllFavoriteBooks()

    suspend fun get(idf: String) = booksDao.get(idf)

    suspend fun saveProgression(locator: Locator, bookIdf: String) =
        booksDao.saveProgression(locator.toJSON().toString(), bookIdf)

    suspend fun insertBookmark(bookIdf: String, publication: Publication, locator: Locator): Long {
        val resource = publication.readingOrder.indexOfFirstWithHref(locator.href)!!
        val bookmark = Bookmark(
            creation = DateTime(DateTime().toDate().time).toString(DateTimeFormat.shortDateTime()),
            bookIdf = bookIdf,
            resourceIndex = resource.toString(),
            resourceHref = locator.href.toString(),
            resourceType = locator.mediaType.toString(),
            resourceTitle = locator.title.orEmpty(),
            location = locator.locations.toJSON().toString(),
            locatorText = Locator.Text().toJSON().toString()
        )

        return booksDao.insertBookmark(bookmark)
    }

    fun bookmarksForBook(bookIdf: String): Flow<List<Bookmark>> =
        booksDao.getBookmarksForBook(bookIdf)

    suspend fun bookmarksDirectlyForBook(bookIdf: String): List<Bookmark> =
        booksDao.getBookmarksDirectlyForBook(bookIdf)

    suspend fun deleteBookmark(bookmarkId: Long) = booksDao.deleteBookmark(bookmarkId)

    suspend fun highlightById(id: Long): Highlight? =
        booksDao.getHighlightById(id)

    fun highlightsForBook(bookIdf: String): Flow<List<Highlight>> =
        booksDao.getHighlightsForBook(bookIdf)

    suspend fun highlightsDirectlyForBook(bookIdf: String): List<Highlight> =
        booksDao.getHighlightsDirectlyForBook(bookIdf)

    suspend fun addHighlight(
        bookIdf: String,
        @ColorInt tint: Int,
        locator: Locator,
        annotation: String
    ): Long =
        booksDao.insertHighlight(Highlight(bookIdf, tint, locator, annotation))

    suspend fun deleteHighlight(id: Long) = booksDao.deleteHighlight(id)

    suspend fun updateHighlightAnnotation(id: Long, annotation: String) {
        booksDao.updateHighlightAnnotation(id, annotation)
    }

    suspend fun updateHighlightStyle(id: Long, @ColorInt tint: Int) {
        booksDao.updateHighlightStyle(id, tint)
    }

    suspend fun insertBook(
        url: Url,
        mediaType: MediaType,
        publication: Publication,
        cover: File
    ): Long {
        val idf = publication.metadata.identifier ?: ""
        if (booksDao.get(idf) != null) {
            return -2
        }
        val book = Book(
            creation = DateTime().toDate().time.toString(),
            title = publication.metadata.title ?: url.filename,
            author = publication.metadata.authors.firstOrNull()?.name ?: "",
            href = url.toString(),
            identifier = idf,
            mediaType = mediaType,
            progression = "{}",
            cover = cover.path
        )
        return booksDao.insertBook(book)
    }

    suspend fun markFavorite(bookIdf: String, favType: Int) {
        booksDao.markFavorite(bookIdf, favType)
    }

    suspend fun markSync(bookIdf: String, syncType: Int) {
        booksDao.markSync(bookIdf, syncType)
    }

    suspend fun deleteBook(bookIdf: String) =
        booksDao.deleteBook(bookIdf)
}
