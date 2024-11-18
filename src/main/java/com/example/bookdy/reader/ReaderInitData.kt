package com.example.bookdy.reader

import org.readium.navigator.media.tts.AndroidTtsNavigatorFactory
import org.readium.navigator.media.tts.android.AndroidTtsPreferences
import org.readium.r2.navigator.epub.EpubNavigatorFactory
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.shared.publication.*
import com.example.bookdy.reader.preferences.PreferencesManager

sealed class ReaderInitData {
    abstract val bookIdf: String
    abstract val publication: Publication
}

sealed class VisualReaderInitData(
    override val bookIdf: String,
    override val publication: Publication,
    val initialLocation: Locator?,
    val ttsInitData: TtsInitData?
) : ReaderInitData()

class EpubReaderInitData(
    bookIdf: String,
    publication: Publication,
    initialLocation: Locator?,
    val preferencesManager: PreferencesManager<EpubPreferences>,
    val navigatorFactory: EpubNavigatorFactory,
    ttsInitData: TtsInitData?
) : VisualReaderInitData(bookIdf, publication, initialLocation, ttsInitData)

class TtsInitData(
    val mediaServiceFacade: MediaServiceFacade,
    val navigatorFactory: AndroidTtsNavigatorFactory,
    val preferencesManager: PreferencesManager<AndroidTtsPreferences>
)

class DummyReaderInitData(
    override val bookIdf: String
) : ReaderInitData() {
    override val publication: Publication = Publication(
        Manifest(
            metadata = Metadata(identifier = "dummy")
        )
    )
}
