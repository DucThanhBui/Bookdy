@file:OptIn(ExperimentalReadiumApi::class)

package com.example.bookdy.reader

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.InvalidatingPagingSourceFactory
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.readium.r2.navigator.Decoration
import org.readium.r2.navigator.HyperlinkNavigator
import org.readium.r2.navigator.epub.EpubNavigatorFragment
import org.readium.r2.shared.ExperimentalReadiumApi
import org.readium.r2.shared.publication.Link
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.LocatorCollection
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.services.search.SearchIterator
import org.readium.r2.shared.publication.services.search.SearchTry
import org.readium.r2.shared.publication.services.search.search
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.Try
import org.readium.r2.shared.util.Url
import org.readium.r2.shared.util.data.ReadError
import com.example.bookdy.Application
import com.example.bookdy.R
import com.example.bookdy.bookshelf.BookshelfViewModel.Companion.TAG
import com.example.bookdy.data.BookRepository
import com.example.bookdy.data.model.Book
import com.example.bookdy.data.model.Highlight
import com.example.bookdy.data.model.HighlightConverters
import com.example.bookdy.data.modeljson.BookJson
import com.example.bookdy.data.modeljson.BookmarkJson
import com.example.bookdy.data.modeljson.HighlightJson
import com.example.bookdy.domain.toUserError
import com.example.bookdy.network.BookApiService
import com.example.bookdy.reader.preferences.UserPreferencesViewModel
import com.example.bookdy.reader.tts.TtsViewModel
import com.example.bookdy.search.SearchPagingSource
import com.example.bookdy.utils.EventChannel
import com.example.bookdy.utils.UserError
import com.example.bookdy.utils.createViewModelFactory
import com.example.bookdy.utils.extensions.toHtml
import com.example.bookdy.utils.global_token
import kotlinx.coroutines.Dispatchers
import retrofit2.HttpException
import timber.log.Timber

@OptIn(ExperimentalReadiumApi::class)
class ReaderViewModel(
    private val application: Application,
    private val bookIdf: String,
    private val readerRepository: ReaderRepository,
    private val bookRepository: BookRepository
) : ViewModel(),
    EpubNavigatorFragment.Listener {

    val readerInitData =
        try {
            checkNotNull(readerRepository[bookIdf])
        } catch (e: Exception) {
            // Fallbacks on a dummy Publication to avoid crashing the app until the Activity finishes.
            DummyReaderInitData(bookIdf)
        }

    var networkStatus = false

    val publication: Publication =
        readerInitData.publication

    val activityChannel: EventChannel<ActivityCommand> =
        EventChannel(Channel(Channel.BUFFERED), viewModelScope)

    val fragmentChannel: EventChannel<FragmentFeedback> =
        EventChannel(Channel(Channel.BUFFERED), viewModelScope)

    val visualFragmentChannel: EventChannel<VisualFragmentCommand> =
        EventChannel(Channel(Channel.BUFFERED), viewModelScope)

    val searchChannel: EventChannel<SearchCommand> =
        EventChannel(Channel(Channel.BUFFERED), viewModelScope)

    val tts: TtsViewModel? = TtsViewModel(
        viewModelScope = viewModelScope,
        readerInitData = readerInitData
    )

    val settings: UserPreferencesViewModel<*, *>? = UserPreferencesViewModel(
        viewModelScope = viewModelScope,
        readerInitData = readerInitData
    )

    override fun onCleared() {
        // When the ReaderViewModel is disposed of, we want to close the publication to avoid
        // using outdated information (such as the initial location) if the `ReaderActivity` is
        // opened again with the same book.
        readerRepository.close(bookIdf)
    }

    fun saveProgression(locator: Locator) = viewModelScope.launch {
        Timber.v("Saving locator for book $bookIdf: $locator.")
        bookRepository.saveProgression(locator, bookIdf)
    }

    fun getBookmarks() = bookRepository.bookmarksForBook(bookIdf)

    fun insertBookmark(locator: Locator) = viewModelScope.launch {
        val id = bookRepository.insertBookmark(bookIdf, publication, locator)
        if (id != -1L) {
            fragmentChannel.send(FragmentFeedback.BookmarkSuccessfullyAdded)
        } else {
            fragmentChannel.send(FragmentFeedback.BookmarkFailed)
        }
    }

    fun doSyncProgression() {
        viewModelScope.launch {
            val book = bookRepository.get(bookIdf)
            if (networkStatus) {
                if (book!!.isSync == 1) {
                    doSync(book)
                } else {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(application.applicationContext, application.applicationContext.getString(R.string.upload_first), Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(application.applicationContext, application.applicationContext.getString(R.string.no_internet), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun deleteBookmark(id: Long) = viewModelScope.launch {
        bookRepository.deleteBookmark(id)
    }
    private fun doSync(book: Book) {
        Log.d(TAG, "doUploadBook")
        viewModelScope.launch(Dispatchers.IO) {
            val bookmarks = bookRepository.bookmarksDirectlyForBook(book.identifier)
            Log.d(TAG, "bookmarks collected is $bookmarks")
            val hls = bookRepository.highlightsDirectlyForBook(book.identifier)
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
            try {
                val response =  BookApiService.retrofitService.updateFileInfo(global_token, bookInfo)
                Handler(Looper.getMainLooper()).post {
                    if (response.status == -1) {
                        Toast.makeText(application.applicationContext, application.applicationContext.getString(R.string.server_error), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(application.applicationContext, application.applicationContext.getString(R.string.sync_success), Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                if (e is HttpException) {
                    Handler(Looper.getMainLooper()).post {
                            Toast.makeText(
                                application.applicationContext,
                                application.applicationContext.getString(R.string.server_error),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    Log.e(TAG, "Exception: ${e.message}")
                }
            }
        }
    }

    // Highlights

    val highlights: Flow<List<Highlight>> by lazy {
        bookRepository.highlightsForBook(bookIdf)
    }

    /**
     * Database ID of the active highlight for the current highlight pop-up. This is used to show
     * the highlight decoration in an "active" state.
     */
    var activeHighlightId = MutableStateFlow<Long?>(null)

    /**
     * Current state of the highlight decorations.
     *
     * It will automatically be updated when the highlights database table or the current
     * [activeHighlightId] change.
     */
    val highlightDecorations: Flow<List<Decoration>> by lazy {
        highlights.combine(activeHighlightId) { highlights, activeId ->
            highlights.flatMap { highlight ->
                highlight.toDecorations(isActive = (highlight.id == activeId))
            }
        }
    }

    /**
     * Creates a list of [Decoration] for the receiver [Highlight].
     */
    private fun Highlight.toDecorations(isActive: Boolean): List<Decoration> {
        fun createDecoration(idSuffix: String, style: Decoration.Style) = Decoration(
            id = "$id-$idSuffix",
            locator = locator,
            style = style,
            extras = mapOf(
                "id" to id
            )
        )

        return listOfNotNull(
            // Decoration for the actual highlight
            createDecoration(
                idSuffix = "highlight",
                style = Decoration.Style.Highlight(
                    tint = tint,
                    isActive = isActive
                )
            ),
            // Additional page margin icon decoration, if the highlight has an associated note.
            annotation.takeIf { it.isNotEmpty() }?.let {
                createDecoration(
                    idSuffix = "annotation",
                    style = DecorationStyleAnnotationMark(tint = tint)
                )
            }
        )
    }

    suspend fun highlightById(id: Long): Highlight? =
        bookRepository.highlightById(id)

    fun addHighlight(
        locator: Locator,
        @ColorInt tint: Int,
        annotation: String = ""
    ) = viewModelScope.launch {
        bookRepository.addHighlight(bookIdf, tint, locator, annotation)
    }

    fun updateHighlightAnnotation(id: Long, annotation: String) = viewModelScope.launch {
        bookRepository.updateHighlightAnnotation(id, annotation)
    }

    fun updateHighlightStyle(id: Long, @ColorInt tint: Int) = viewModelScope.launch {
        bookRepository.updateHighlightStyle(id, tint)
    }

    fun deleteHighlight(id: Long) = viewModelScope.launch {
        bookRepository.deleteHighlight(id)
    }

    // Search

    fun search(query: String) = viewModelScope.launch {
        if (query == lastSearchQuery) return@launch
        lastSearchQuery = query
        _searchLocators.value = emptyList()
        searchIterator = publication.search(query)
            ?: run {
                activityChannel.send(
                    ActivityCommand.ToastError(
                        UserError(R.string.search_error_not_searchable, cause = null)
                    )
                )
                null
            }
        pagingSourceFactory.invalidate()
        searchChannel.send(SearchCommand.StartNewSearch)
    }

    fun cancelSearch() = viewModelScope.launch {
        _searchLocators.value = emptyList()
        searchIterator?.close()
        searchIterator = null
        pagingSourceFactory.invalidate()
    }

    val searchLocators: StateFlow<List<Locator>> get() = _searchLocators
    private var _searchLocators = MutableStateFlow<List<Locator>>(emptyList())

    /**
     * Maps the current list of search result locators into a list of [Decoration] objects to
     * underline the results in the navigator.
     */
    val searchDecorations: Flow<List<Decoration>> by lazy {
        searchLocators.map {
            it.mapIndexed { index, locator ->
                Decoration(
                    id = index.toString(),
                    locator = locator,
                    style = Decoration.Style.Underline(tint = Color.RED)
                )
            }
        }
    }

    private var lastSearchQuery: String? = null

    private var searchIterator: SearchIterator? = null

    private val pagingSourceFactory = InvalidatingPagingSourceFactory {
        SearchPagingSource(listener = PagingSourceListener())
    }


    override fun onResourceLoadFailed(href: Url, error: ReadError) {
        activityChannel.send(
            ActivityCommand.ToastError(error.toUserError())
        )
    }

    override fun onExternalLinkActivated(url: AbsoluteUrl) {
    }

    override fun shouldFollowInternalLink(
        link: Link,
        context: HyperlinkNavigator.LinkContext?
    ): Boolean =
        when (context) {
            is HyperlinkNavigator.FootnoteContext -> {
                val text =
                    if (link.mediaType?.isHtml == true) {
                        context.noteContent.toHtml()
                    } else {
                        context.noteContent
                    }

                val command = VisualFragmentCommand.ShowPopup(text)
                visualFragmentChannel.send(command)
                false
            }
            else -> true
        }

    // Search

    inner class PagingSourceListener : SearchPagingSource.Listener {
        override suspend fun next(): SearchTry<LocatorCollection?> {
            val iterator = searchIterator ?: return Try.success(null)
            return iterator.next().onSuccess {
                _searchLocators.value += (it?.locators ?: emptyList())
            }
        }
    }

    val searchResult: Flow<PagingData<Locator>> =
        Pager(PagingConfig(pageSize = 20), pagingSourceFactory = pagingSourceFactory)
            .flow.cachedIn(viewModelScope)


    sealed class ActivityCommand {
        data object OpenOutlineRequested : ActivityCommand()
        class ToastError(val error: UserError) : ActivityCommand()
    }

    sealed class FragmentFeedback {
        data object BookmarkSuccessfullyAdded : FragmentFeedback()
        data object BookmarkFailed : FragmentFeedback()
    }

    sealed class VisualFragmentCommand {
        class ShowPopup(val text: CharSequence) : VisualFragmentCommand()
    }

    sealed class SearchCommand {
        data object StartNewSearch : SearchCommand()
    }

    companion object {
        fun createFactory(application: Application, arguments: ReaderActivityContract.Arguments) =
            createViewModelFactory {
                ReaderViewModel(
                    application,
                    arguments.bookIdf,
                    application.readerRepository,
                    application.bookRepository
                )
            }
    }
}
