package com.example.bookdy.domain

import android.net.Uri
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.DebugError
import org.readium.r2.shared.util.Try
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.file.FileSystemError
import org.readium.r2.shared.util.format.Format
import org.readium.r2.shared.util.getOrElse
import org.readium.r2.shared.util.toUrl
import org.readium.r2.streamer.PublicationOpener
import com.example.bookdy.data.BookRepository
import com.example.bookdy.data.model.Book
import com.example.bookdy.utils.tryOrLog
import timber.log.Timber

class Bookshelf(
    private val bookRepository: BookRepository,
    private val coverStorage: CoverStorage,
    private val publicationOpener: PublicationOpener,
    private val assetRetriever: AssetRetriever,
    private val publicationRetriever: PublicationRetriever
) {
    sealed class Event {
        data object ImportPublicationSuccess :
            Event()

        class ImportPublicationError(
            val error: ImportError
        ) : Event()
    }

    val channel: Channel<Event> =
        Channel(Channel.UNLIMITED)

    private val coroutineScope: CoroutineScope =
        MainScope()

    fun importPublicationFromStorage(
        uri: Uri
    ) {
        coroutineScope.launch {
            addBookFeedback(publicationRetriever.retrieveFromStorage(uri))
        }
    }

    fun addPublicationFromStorage(
        url: AbsoluteUrl
    ) {
        coroutineScope.launch {
            addBookFeedback(url)
        }
    }

    private suspend fun addBookFeedback(
        retrieverResult: Try<PublicationRetriever.Result, ImportError>
    ) {
        retrieverResult
            .map { addBook(it.publication.toUrl(), it.format, it.coverUrl) }
            .onSuccess { channel.send(Event.ImportPublicationSuccess) }
            .onFailure { channel.send(Event.ImportPublicationError(it)) }
    }

    private suspend fun addBookFeedback(
        url: AbsoluteUrl,
        format: Format? = null,
        coverUrl: AbsoluteUrl? = null
    ) {
        addBook(url, format, coverUrl)
            .onSuccess { channel.send(Event.ImportPublicationSuccess) }
            .onFailure { channel.send(Event.ImportPublicationError(it)) }
    }

    private suspend fun addBook(
        url: AbsoluteUrl,
        format: Format? = null,
        coverUrl: AbsoluteUrl? = null
    ): Try<Unit, ImportError> {
        val asset =
            if (format == null) {
                assetRetriever.retrieve(url)
            } else {
                assetRetriever.retrieve(url, format)
            }.getOrElse {
                return Try.failure(
                    ImportError.Publication(PublicationError(it))
                )
            }

        publicationOpener.open(
            asset,
            allowUserInteraction = false
        ).onSuccess { publication ->
            val coverFile =
                coverStorage.storeCover(publication, coverUrl)
                    .getOrElse {
                        return Try.failure(
                            ImportError.FileSystem(
                                FileSystemError.IO(it)
                            )
                        )
                    }

            val id = bookRepository.insertBook(
                url,
                asset.format.mediaType,
                publication,
                coverFile
            )
            if (id == -2L) {
                coverFile.delete()
                return Try.failure(
                    ImportError.Database(
                        DebugError("Book already existed.")
                    )
                )
            }
            if (id == -1L) {
                coverFile.delete()
                return Try.failure(
                    ImportError.Database(
                        DebugError("Could not insert book into database.")
                    )
                )
            }
        }
            .onFailure {
                Timber.e("Cannot open publication: $it.")
                return Try.failure(
                    ImportError.Publication(PublicationError(it))
                )
            }

        return Try.success(Unit)
    }

    suspend fun markFavorite(book: Book, favType: Int) {
        val idf = book.identifier
        bookRepository.markFavprite(idf, favType)
    }

    suspend fun deleteBook(book: Book) {
        val idf = book.identifier
        bookRepository.deleteBook(idf)
        tryOrLog { book.url.toFile()?.delete() }
        tryOrLog { File(book.cover).delete() }
    }
}
