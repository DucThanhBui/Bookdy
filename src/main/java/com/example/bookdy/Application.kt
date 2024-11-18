package com.example.bookdy

import android.content.Context
import android.os.Build
import android.os.StrictMode
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.bookdy.BuildConfig.DEBUG
import com.google.android.material.color.DynamicColors
import java.io.File
import java.util.Properties
import java.util.concurrent.Executors
import com.example.bookdy.data.BookRepository
import com.example.bookdy.data.db.AppDatabase
import com.example.bookdy.domain.Bookshelf
import com.example.bookdy.domain.CoverStorage
import com.example.bookdy.domain.PublicationRetriever
import com.example.bookdy.reader.ReaderRepository
import com.example.bookdy.utils.tryOrLog
import timber.log.Timber

class Application : android.app.Application() {

    lateinit var publicationHelper: PublicationHelper
        private set

    lateinit var storageDir: File

    lateinit var bookRepository: BookRepository
        private set

    lateinit var bookshelf: Bookshelf
        private set

    lateinit var readerRepository: ReaderRepository
        private set

    private val Context.navigatorPreferences: DataStore<Preferences>
        by preferencesDataStore(name = "navigator-preferences")

    override fun onCreate() {
        super.onCreate()

        DynamicColors.applyToActivitiesIfAvailable(this)

        publicationHelper = PublicationHelper(this)

        storageDir = computeStorageDir()

        val database = AppDatabase.getDatabase(this)

        bookRepository = BookRepository(database.booksDao())

        val downloadsDir = File(cacheDir, "downloads")

        // Cleans the download dir.
        tryOrLog { downloadsDir.delete() }

        val publicationRetriever =
            PublicationRetriever(
                context = applicationContext,
                assetRetriever = publicationHelper.assetRetriever,
                bookshelfDir = storageDir,
                tempDir = downloadsDir,
                httpClient = publicationHelper.httpClient
            )

        bookshelf =
            Bookshelf(
                bookRepository,
                CoverStorage(storageDir),
                publicationHelper.publicationOpener,
                publicationHelper.assetRetriever,
                publicationRetriever
            )

        readerRepository = ReaderRepository(
            this@Application,
            publicationHelper,
            bookRepository,
            navigatorPreferences
        )
    }

    private fun computeStorageDir(): File {
        val properties = Properties()
        val inputStream = assets.open("configs/config.properties")
        properties.load(inputStream)
        val useExternalFileDir =
            properties.getProperty("useExternalFileDir", "false")!!.toBoolean()

        return File(
            if (useExternalFileDir) {
                getExternalFilesDir(null)?.path + "/"
            } else {
                filesDir?.path + "/"
            }
        )
    }
}
