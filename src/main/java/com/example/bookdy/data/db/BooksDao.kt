package com.example.bookdy.data.db

import androidx.annotation.ColorInt
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.example.bookdy.data.model.Book
import com.example.bookdy.data.model.Bookmark
import com.example.bookdy.data.model.Highlight

@Dao
interface BooksDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book): Long

    @Query("DELETE FROM " + Book.TABLE_NAME + " WHERE " + Book.IDENTIFIER + " = :bookIdf")
    suspend fun deleteBook(bookIdf: String)

    @Query("SELECT * FROM " + Book.TABLE_NAME + " WHERE " + Book.IDENTIFIER + " = :bookIdf")
    suspend fun get(bookIdf: String): Book?

    @Query("SELECT * FROM " + Book.TABLE_NAME + " ORDER BY " + Book.CREATION_DATE + "*1 desc")
    fun getAllBooks(): Flow<List<Book>>

    @Query("SELECT * FROM " + Book.TABLE_NAME + " WHERE " + Book.FAVORITE + " = 1 ORDER BY " + Book.CREATION_DATE + "*1 desc")
    fun getAllFavoriteBooks(): Flow<List<Book>>

    @Query("SELECT * FROM " + Bookmark.TABLE_NAME + " WHERE " + Bookmark.BOOK_IDF + " = :bookIdf")
    fun getBookmarksForBook(bookIdf: String): Flow<List<Bookmark>>

    @Query(
        "SELECT * FROM ${Highlight.TABLE_NAME} WHERE ${Highlight.BOOK_IDF} = :bookIdf ORDER BY ${Highlight.TOTAL_PROGRESSION} ASC"
    )
    fun getHighlightsForBook(bookIdf: String): Flow<List<Highlight>>

    @Query("SELECT * FROM ${Highlight.TABLE_NAME} WHERE ${Highlight.ID} = :highlightId")
    suspend fun getHighlightById(highlightId: Long): Highlight?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBookmark(bookmark: Bookmark): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHighlight(highlight: Highlight): Long

    @Query(
        "UPDATE ${Highlight.TABLE_NAME} SET ${Highlight.ANNOTATION} = :annotation WHERE ${Highlight.ID} = :id"
    )
    suspend fun updateHighlightAnnotation(id: Long, annotation: String)

    @Query(
        "UPDATE ${Highlight.TABLE_NAME} SET ${Highlight.TINT} = :tint WHERE ${Highlight.ID} = :id"
    )
    suspend fun updateHighlightStyle(id: Long, @ColorInt tint: Int)

    @Query("DELETE FROM " + Bookmark.TABLE_NAME + " WHERE " + Bookmark.ID + " = :id")
    suspend fun deleteBookmark(id: Long)

    @Query("DELETE FROM ${Highlight.TABLE_NAME} WHERE ${Highlight.ID} = :id")
    suspend fun deleteHighlight(id: Long)

    @Query(
        "UPDATE " + Book.TABLE_NAME + " SET " + Book.PROGRESSION + " = :locator WHERE " + Book.IDENTIFIER + "= :bookIdf"
    )
    suspend fun saveProgression(locator: String, bookIdf: String)

    @Query(
        "UPDATE " + Book.TABLE_NAME + " SET " + Book.FAVORITE + " = :favType WHERE " + Book.IDENTIFIER + "= :bookIdf"
    )
    suspend fun markFavorite(bookIdf: String, favType: Int)
}
