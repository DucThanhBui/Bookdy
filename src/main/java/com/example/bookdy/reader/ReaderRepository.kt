package com.example.bookdy.reader

import android.app.Application
import androidx.datastore.core.DataStore
import com.example.bookdy.PublicationHelper
import com.example.bookdy.R
import androidx.datastore.preferences.core.Preferences as JetpackPreferences
import org.json.JSONObject
import org.readium.navigator.media.tts.TtsNavigatorFactory
import org.readium.r2.navigator.epub.EpubNavigatorFactory
import org.readium.r2.shared.ExperimentalReadiumApi
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.allAreHtml
import org.readium.r2.shared.util.DebugError
import org.readium.r2.shared.util.Try
import org.readium.r2.shared.util.getOrElse
import com.example.bookdy.data.BookRepository
import com.example.bookdy.reader.preferences.AndroidTtsPreferencesManagerFactory
import com.example.bookdy.reader.preferences.EpubPreferencesManagerFactory
import com.example.bookdy.utils.CoroutineQueue
import timber.log.Timber
import com.example.bookdy.domain.PublicationError

@OptIn(ExperimentalReadiumApi::class)
class ReaderRepository(
    private val application: Application,
    private val publicationHelper: PublicationHelper,
    private val bookRepository: BookRepository,
    private val preferencesDataStore: DataStore<JetpackPreferences>
) {

    private val coroutineQueue: CoroutineQueue =
        CoroutineQueue()

    private val repository: MutableMap<String, ReaderInitData> =
        mutableMapOf()

    private val mediaServiceFacade: MediaServiceFacade =
        MediaServiceFacade(application)

    fun isEmpty() =
        repository.isEmpty()

    operator fun get(bookIdf: String): ReaderInitData? =
        repository[bookIdf]

    suspend fun open(bookIdf: String): Try<Unit, OpeningError> =
        coroutineQueue.await { doOpen(bookIdf) }

    private suspend fun doOpen(bookIdf: String): Try<Unit, OpeningError> {
        if (bookIdf in repository.keys) {
            return Try.success(Unit)
        }
        val book = checkNotNull(bookRepository.get(bookIdf)) { "Cannot find book in database." }
        val asset = publicationHelper.assetRetriever.retrieve(
            book.url,
            book.mediaType
        ).getOrElse {
            return Try.failure(
                OpeningError.PublicationError(
                    PublicationError(it)
                )
            )
        }

        val publication = publicationHelper.publicationOpener.open(
            asset,
            allowUserInteraction = true
        ).getOrElse {
            return Try.failure(
                OpeningError.PublicationError(
                    PublicationError(it)
                )
            )
        }

        val initialLocator = book.progression
            ?.let { Locator.fromJSON(JSONObject(it)) }

        val readerInitData = when {
            publication.conformsTo(Publication.Profile.EPUB) || publication.readingOrder.allAreHtml ->
                openEpub(bookIdf, publication, initialLocator)
            else ->
                Try.failure(
                    OpeningError.CannotRender(
                        DebugError(application.applicationContext.getString(R.string.no_support_this_file))
                    )
                )
        }

        return readerInitData.map {
            repository[bookIdf] = it
        }
    }


    private suspend fun openEpub(
        bookIdf: String,
        publication: Publication,
        initialLocator: Locator?
    ): Try<EpubReaderInitData, OpeningError> {
        val preferencesManager = EpubPreferencesManagerFactory(preferencesDataStore)
            .createPreferenceManager(bookIdf)
        val navigatorFactory = EpubNavigatorFactory(publication)
        val ttsInitData = getTtsInitData(bookIdf, publication)

        val initData = EpubReaderInitData(
            bookIdf,
            publication,
            initialLocator,
            preferencesManager,
            navigatorFactory,
            ttsInitData
        )
        return Try.success(initData)
    }

    private suspend fun getTtsInitData(
        bookIdf: String,
        publication: Publication
    ): TtsInitData? {
        val preferencesManager = AndroidTtsPreferencesManagerFactory(preferencesDataStore)
            .createPreferenceManager(bookIdf)
        val navigatorFactory = TtsNavigatorFactory(
            application,
            publication
        ) ?: return null
        return TtsInitData(mediaServiceFacade, navigatorFactory, preferencesManager)
    }

    fun close(bookIdf: String) {
        coroutineQueue.launch {
            Timber.v("Closing Publication $bookIdf.")
            when (val initData = repository.remove(bookIdf)) {
                is VisualReaderInitData -> {
                    mediaServiceFacade.closeSession()
                    initData.publication.close()
                }
                null, is DummyReaderInitData -> {
                }
            }
        }
    }
}
