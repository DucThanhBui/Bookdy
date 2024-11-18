package com.example.bookdy.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.bookdy.data.model.Book
import com.example.bookdy.data.model.Bookmark
import com.example.bookdy.data.model.Highlight
import com.example.bookdy.data.model.HighlightConverters
import com.example.bookdy.data.db.BooksDao

@Database(
    entities = [Book::class, Bookmark::class, Highlight::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    HighlightConverters::class
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun booksDao(): BooksDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
