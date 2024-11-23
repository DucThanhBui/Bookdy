@file:OptIn(ExperimentalReadiumApi::class)

package com.example.bookdy.reader.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlin.reflect.KClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferences
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferencesSerializer
import org.readium.adapter.pdfium.navigator.PdfiumPreferences
import org.readium.adapter.pdfium.navigator.PdfiumPreferencesSerializer
import org.readium.adapter.pdfium.navigator.PdfiumPublicationPreferencesFilter
import org.readium.adapter.pdfium.navigator.PdfiumSharedPreferencesFilter
import org.readium.navigator.media.tts.android.AndroidTtsPreferences
import org.readium.navigator.media.tts.android.AndroidTtsPreferencesSerializer
import org.readium.navigator.media.tts.android.AndroidTtsPublicationPreferencesFilter
import org.readium.navigator.media.tts.android.AndroidTtsSharedPreferencesFilter
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.navigator.epub.EpubPreferencesSerializer
import org.readium.r2.navigator.epub.EpubPublicationPreferencesFilter
import org.readium.r2.navigator.epub.EpubSharedPreferencesFilter
import org.readium.r2.navigator.preferences.Configurable
import org.readium.r2.navigator.preferences.PreferencesFilter
import org.readium.r2.navigator.preferences.PreferencesSerializer
import org.readium.r2.shared.ExperimentalReadiumApi
import com.example.bookdy.utils.extensions.stateInFirst
import com.example.bookdy.utils.tryOrNull

class PreferencesManager<P : Configurable.Preferences<P>> internal constructor(
    val preferences: StateFlow<P>,
    @Suppress("Unused")
    private val coroutineScope: CoroutineScope,
    private val editPreferences: suspend (P) -> Unit
) {

    suspend fun setPreferences(preferences: P) {
        editPreferences(preferences)
    }
}

sealed class PreferencesManagerFactory<P : Configurable.Preferences<P>>(
    private val dataStore: DataStore<Preferences>,
    private val klass: KClass<P>,
    private val sharedPreferencesFilter: PreferencesFilter<P>,
    private val publicationPreferencesFilter: PreferencesFilter<P>,
    private val preferencesSerializer: PreferencesSerializer<P>,
    private val emptyPreferences: P
) {
    suspend fun createPreferenceManager(bookIdf: String): PreferencesManager<P> {
        val coroutineScope = CoroutineScope(Dispatchers.IO)
        val preferences = getPreferences(bookIdf, coroutineScope)

        return PreferencesManager(
            preferences = preferences,
            coroutineScope = coroutineScope,
            editPreferences = { setPreferences(bookIdf, it) }
        )
    }

    private suspend fun setPreferences(bookIdf: String, preferences: P) {
        dataStore.edit { data ->
            data[key(klass)] = sharedPreferencesFilter
                .filter(preferences)
                .let { preferencesSerializer.serialize(it) }
        }

        dataStore.edit { data ->
            data[key(bookIdf)] = publicationPreferencesFilter
                .filter(preferences)
                .let { preferencesSerializer.serialize(it) }
        }
    }

    private suspend fun getPreferences(bookIdf: String, scope: CoroutineScope): StateFlow<P> {
        val sharedPrefs = dataStore.data
            .map { data -> data[key(klass)] }
            .map { json ->
                tryOrNull {
                    json?.let { preferencesSerializer.deserialize(it) }
                } ?: emptyPreferences
            }

        val pubPrefs = dataStore.data
            .map { data -> data[key(bookIdf)] }
            .map { json ->
                tryOrNull {
                    json?.let { preferencesSerializer.deserialize(it) }
                } ?: emptyPreferences
            }

        return combine(sharedPrefs, pubPrefs) { shared, pub -> shared + pub }
            .stateInFirst(scope, SharingStarted.Eagerly)
    }

    /** [DataStore] key for the given [bookId]. */
    private fun key(bookIdf: String): Preferences.Key<String> =
        stringPreferencesKey("book-$bookIdf")

    /** [DataStore] key for the given preferences [klass]. */
    private fun <T : Any> key(klass: KClass<T>): Preferences.Key<String> =
        stringPreferencesKey("class-${klass.simpleName}")
}

class EpubPreferencesManagerFactory(
    dataStore: DataStore<Preferences>
) : PreferencesManagerFactory<EpubPreferences>(
    dataStore = dataStore,
    klass = EpubPreferences::class,
    sharedPreferencesFilter = EpubSharedPreferencesFilter,
    publicationPreferencesFilter = EpubPublicationPreferencesFilter,
    preferencesSerializer = EpubPreferencesSerializer(),
    emptyPreferences = EpubPreferences()
)

class AndroidTtsPreferencesManagerFactory(
    dataStore: DataStore<Preferences>
) : PreferencesManagerFactory<AndroidTtsPreferences>(
    dataStore = dataStore,
    klass = AndroidTtsPreferences::class,
    sharedPreferencesFilter = AndroidTtsSharedPreferencesFilter,
    publicationPreferencesFilter = AndroidTtsPublicationPreferencesFilter,
    preferencesSerializer = AndroidTtsPreferencesSerializer(),
    emptyPreferences = AndroidTtsPreferences()
)
