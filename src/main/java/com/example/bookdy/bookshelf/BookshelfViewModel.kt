package com.example.bookdy.bookshelf

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.example.bookdy.R
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.toUrl
import com.example.bookdy.data.model.Book
import com.example.bookdy.data.model.Bookmark
import com.example.bookdy.data.model.Highlight
import com.example.bookdy.data.model.HighlightConverters
import com.example.bookdy.data.modeljson.BookJson
import com.example.bookdy.data.modeljson.BookmarkJson
import com.example.bookdy.data.modeljson.HighlightJson
import com.example.bookdy.data.modeljson.ResponseMessage
import com.example.bookdy.network.BookApi
import com.example.bookdy.network.BookApiService
import com.example.bookdy.reader.OpeningError
import com.example.bookdy.reader.ReaderActivityContract
import com.example.bookdy.utils.EventChannel
import com.example.bookdy.utils.extensions.flattenToList
import com.example.bookdy.utils.global_token
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class BookshelfViewModel(application: Application) : AndroidViewModel(application) {

    val app get() =
        getApplication<com.example.bookdy.Application>()

    val channel = EventChannel(Channel<Event>(Channel.BUFFERED), viewModelScope)
    val books = app.bookRepository.books()
    val favoriteBooks = app.bookRepository.favoriteBooks()

    var networkStatus = false

    fun deletePublication(book: Book) =
        viewModelScope.launch {
            app.bookshelf.deleteBook(book)
        }

    fun markFavorite(book: Book, favType: Int) {
        viewModelScope.launch {
            app.bookshelf.markFavorite(book, favType)
        }
    }

    fun markSync(book: Book, syncType: Int) {
        viewModelScope.launch {
            app.bookshelf.markSync(book, syncType)
        }
    }

    fun showNetworkStatus() {
        if (!networkStatus) {
            Toast.makeText(getApplication(), app.applicationContext.getString(R.string.no_internet), Toast.LENGTH_SHORT).show()
        }
    }

    fun importPublicationFromStorage(uri: Uri) {
        app.bookshelf.importPublicationFromStorage(uri)
    }

    fun addPublicationFromStorage(uri: Uri) {
        app.bookshelf.addPublicationFromStorage(uri.toUrl()!! as AbsoluteUrl)
    }

    fun openPublication(
        bookIdf: String
    ) {
        viewModelScope.launch {
            app.readerRepository
                .open(bookIdf)
                .onFailure {
                    channel.send(Event.OpenPublicationError(it))
                }
                .onSuccess {
                    val arguments = ReaderActivityContract.Arguments(bookIdf)
                    channel.send(Event.LaunchReader(arguments))
                }
        }
    }

    fun openPublicationDirectly(
        bookIdf: String,
        context: Context
    ) {
        viewModelScope.launch {
            app.readerRepository
                .open(bookIdf)
                .onFailure {
                    Toast.makeText(context, context.getString(R.string.opening_error), Toast.LENGTH_SHORT).show()
                }
                .onSuccess {
                    val intent = ReaderActivityContract().createIntent(
                        context,
                        ReaderActivityContract.Arguments(bookIdf)
                    )
                    context.startActivity(intent)
                }
        }
    }

    @SuppressLint("LogNotTimber")
    private suspend fun uploadFiles(fileUri: String, coverPath: String, fileInfo: BookJson): ResponseMessage {
        // Create MultipartBody.Part for each file
        val filerUri = Uri.parse(fileUri)
        val filePath = filerUri.path!!
        Log.d(TAG, "file path is $filePath")
        val file = File(filePath)
        val fileType = "application/octet-stream"
        Log.d(TAG, "file type is $fileType")
        val fileMediaType = fileType.toMediaTypeOrNull()
        val requestFile = RequestBody.create(fileMediaType, file)
        Log.d(TAG, "filename is ${file.name}")
        val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
        val response = BookApiService.retrofitService.uploadBook(filePart)
        Handler(Looper.getMainLooper()).post {
            if (response.status == -1) {
                Toast.makeText(app.applicationContext, app.applicationContext.getString(R.string.upload_fail), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(app.applicationContext, app.applicationContext.getString(R.string.upload_success), Toast.LENGTH_SHORT).show()
            }
        }
        var pathOnServer: String = ""
        if (response.status != -1) pathOnServer = response.message
        else return response

        Log.d(TAG, "pathOnServer is $pathOnServer")


        Log.d(TAG, "cover path is $coverPath")
        val cover = File(coverPath)
        val requestCover = RequestBody.create(fileMediaType, cover)
        Log.d(TAG, "cover filename is ${cover.name}")
        val coverPart = MultipartBody.Part.createFormData("file", cover.name, requestCover)
        val response2 = BookApiService.retrofitService.uploadBook(coverPart)
        Handler(Looper.getMainLooper()).post {
            if (response2.status == -1) {
                Toast.makeText(app.applicationContext, app.applicationContext.getString(R.string.upload_fail), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(app.applicationContext, app.applicationContext.getString(R.string.upload_success), Toast.LENGTH_SHORT).show()
            }
        }
        var coverOnServer: String = ""
        if (response2.status != -1) coverOnServer = response2.message
        else return response2
        Log.d(TAG, "coverOnServer is $coverOnServer")

        // Make the API call
        Log.d("Readiumxxx", "call API")
        fileInfo.pathOnServer = pathOnServer.replace("\\", "/")
        fileInfo.cover = coverOnServer.replace("\\", "/")
        Log.d(TAG, "fileInfo is $fileInfo")
        return BookApiService.retrofitService.uploadBookInfo(token = global_token, bookInfo = fileInfo)
    }

    @SuppressLint("LogNotTimber")
    fun doUploadBook(book: Book) {
        Log.d(TAG, "doUploadBook")
        viewModelScope.launch(Dispatchers.IO) {
            val bookmarks = app.bookRepository.bookmarksDirectlyForBook(book.identifier)
            Log.d(TAG, "bookmarks collected is $bookmarks")
            val hls = app.bookRepository.highlightsDirectlyForBook(book.identifier)
            Log.d(TAG, "highlights collected is $hls")

            val bookmarksJson = mutableListOf<BookmarkJson>()
            val highlightsJson = mutableListOf<HighlightJson>()
            bookmarks.forEach {
                Log.e(TAG, "boomark loc: " + it.location)
                val bmJson = BookmarkJson(
                    creation = it.creation,
                    bookId = it.bookIdf,
                    resourceIndex = it.resourceIndex,
                    resourceHref = it.resourceHref,
                    resourceType = it.resourceType,
                    resourceTitle = it.resourceTitle,
                    locatorText = it.locatorText,
                    location = it.location
                )
                Log.e(TAG, "bmjson loc is ${bmJson.location}")
                Log.e(TAG, "bookmarkJson add to list is $bmJson")
                bookmarksJson.add(bmJson)
            }
            val hlconverter = HighlightConverters()
            hls.forEach {
                val location = hlconverter.locationsToString(it.locations)
                Log.e(TAG, "hightlight loc: $location")
                val hlJson = HighlightJson(
                    bookId = it.bookIdf,
                    tint = it.tint,
                    href = it.href,
                    type = it.type,
                    title = it.title,
                    totalProgression = it.totalProgression.toString(),
                    annotation = it.annotation,
                    text = hlconverter.textToString(it.text),
                    location = location
                )
                Log.e(TAG, "hlJson add to list is $hlJson")
                highlightsJson.add(hlJson)
            }
            val bookInfo = BookJson(
                creation = book.creation!!,
                filename = book.title,
                identifier = book.identifier,
                author = book.author,
                progression = book.progression,
                rawMediaType = book.rawMediaType,
                bookmarks = bookmarksJson,
                highlights = highlightsJson
            )
            Log.d("Readiumxxx", "dat/a ready")
            val response =  uploadFiles(book.href, book.cover, bookInfo)
            Log.d(TAG, "response is ${response.status}, ${response.message}")
            //if (response.status != -1) markSync(book, SYNC_ED)
            Handler(Looper.getMainLooper()).post {
                if (response.status == -1) {
                    Toast.makeText(app.applicationContext, app.applicationContext.getString(R.string.server_error), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(app.applicationContext, app.applicationContext.getString(R.string.upload_success), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        const val TAG = "Readiumxxx"
        const val NOT_SYNC = 0
        const val SYNC_ED = 1
    }

    sealed class Event {

        class OpenPublicationError(
            val error: OpeningError
        ) : Event()

        class LaunchReader(
            val arguments: ReaderActivityContract.Arguments
        ) : Event()
    }
}
