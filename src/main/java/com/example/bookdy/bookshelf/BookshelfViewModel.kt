package com.example.bookdy.bookshelf

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookdy.R
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.toUrl
import com.example.bookdy.data.model.Book
import com.example.bookdy.reader.OpeningError
import com.example.bookdy.reader.ReaderActivityContract
import com.example.bookdy.utils.EventChannel

class BookshelfViewModel(application: Application) : AndroidViewModel(application) {

    private val app get() =
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

    sealed class Event {

        class OpenPublicationError(
            val error: OpeningError
        ) : Event()

        class LaunchReader(
            val arguments: ReaderActivityContract.Arguments
        ) : Event()
    }
}
