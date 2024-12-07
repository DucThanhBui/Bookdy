@file:OptIn(ExperimentalReadiumApi::class)

package com.example.bookdy.reader.preferences

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferencesEditor
import org.readium.adapter.pdfium.navigator.PdfiumPreferencesEditor
import org.readium.navigator.media.tts.android.AndroidTtsEngine
import org.readium.r2.navigator.epub.EpubPreferencesEditor
import org.readium.r2.navigator.preferences.*
import org.readium.r2.navigator.preferences.TextAlign as ReadiumTextAlign
import org.readium.r2.shared.ExperimentalReadiumApi
import org.readium.r2.shared.publication.epub.EpubLayout
import org.readium.r2.shared.util.Language
import com.example.bookdy.LITERATA
import com.example.bookdy.R
import com.example.bookdy.reader.ReaderViewModel
import com.example.bookdy.reader.tts.TtsPreferencesEditor
import com.example.bookdy.shared.views.*
import com.example.bookdy.utils.compose.DropdownMenuButton

/**
 * Stateful user settings component paired with a [ReaderViewModel].
 */
@Composable
fun UserPreferences(
    model: UserPreferencesViewModel<*, *>,
    title: String
) {
    val editor by model.editor.collectAsState()

    UserPreferences(
        editor = editor,
        commit = model::commit,
        title = title
    )
}

@Composable
private fun <P : Configurable.Preferences<P>, E : PreferencesEditor<P>> UserPreferences(
    editor: E,
    commit: () -> Unit,
    title: String
) {
    Column(
        modifier = Modifier.padding(vertical = 24.dp)
    ) {
        Text(
            text = title,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.End),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { editor.clear(); commit() }
            ) {
                Text(stringResource(R.string.reset))
            }
        }

        Divider()

        when (editor) {
            is PdfiumPreferencesEditor ->
                FixedLayoutUserPreferences(
                    commit = commit,
                    readingProgression = editor.readingProgression,
                    scrollAxis = editor.scrollAxis,
                    fit = editor.fit,
                    pageSpacing = editor.pageSpacing
                )

            is EpubPreferencesEditor ->
                when (editor.layout) {
                    EpubLayout.REFLOWABLE ->
                        ReflowableUserPreferences(
                            commit = commit,
                            backgroundColor = editor.backgroundColor,
                            columnCount = editor.columnCount,
                            fontFamily = editor.fontFamily,
                            fontSize = editor.fontSize,
                            fontWeight = editor.fontWeight,
                            hyphens = editor.hyphens,
                            imageFilter = editor.imageFilter,
                            language = editor.language,
                            letterSpacing = editor.letterSpacing,
                            ligatures = editor.ligatures,
                            lineHeight = editor.lineHeight,
                            pageMargins = editor.pageMargins,
                            paragraphIndent = editor.paragraphIndent,
                            paragraphSpacing = editor.paragraphSpacing,
                            publisherStyles = editor.publisherStyles,
                            readingProgression = editor.readingProgression,
                            scroll = editor.scroll,
                            textAlign = editor.textAlign,
                            textColor = editor.textColor,
                            textNormalization = editor.textNormalization,
                            theme = editor.theme,
                            typeScale = editor.typeScale,
                            verticalText = editor.verticalText,
                            wordSpacing = editor.wordSpacing
                        )
                    EpubLayout.FIXED ->
                        FixedLayoutUserPreferences(
                            commit = commit,
                            backgroundColor = editor.backgroundColor,
                            language = editor.language,
                            readingProgression = editor.readingProgression,
                            spread = editor.spread
                        )
                }
            is TtsPreferencesEditor ->
                MediaUserPreferences(
                    commit = commit,
                    language = editor.language,
                    voice = editor.voice,
                    speed = editor.speed,
                    pitch = editor.pitch
                )
            is ExoPlayerPreferencesEditor ->
                MediaUserPreferences(
                    commit = commit,
                    speed = editor.speed,
                    pitch = editor.pitch
                )
        }
    }
}

@Composable
private fun MediaUserPreferences(
    commit: () -> Unit,
    language: Preference<Language?>? = null,
    voice: EnumPreference<AndroidTtsEngine.Voice.Id?>? = null,
    speed: RangePreference<Double>? = null,
    pitch: RangePreference<Double>? = null
) {
    Column {
        if (speed != null) {
            StepperItem(
                title = stringResource(R.string.speed_rate),
                preference = speed,
                commit = commit
            )
        }

        if (pitch != null) {
            StepperItem(
                title = stringResource(R.string.pitch_rate),
                preference = pitch,
                commit = commit
            )
        }
        if (language != null) {
            LanguageItem(
                preference = language,
                commit = commit
            )
        }

        if (voice != null) {
            MenuItem(
                title = stringResource(R.string.tts_voice),
                preference = voice,
                formatValue = { it?.value ?: "Default" },
                commit = commit
            )
        }
    }
}

/**
 * User settings for a publication with a fixed layout, such as fixed-layout EPUB, PDF or comic book.
 */
@Composable
private fun FixedLayoutUserPreferences(
    commit: () -> Unit,
    language: Preference<Language?>? = null,
    readingProgression: EnumPreference<ReadingProgression>? = null,
    backgroundColor: Preference<Color>? = null,
    scroll: Preference<Boolean>? = null,
    scrollAxis: EnumPreference<Axis>? = null,
    fit: EnumPreference<Fit>? = null,
    spread: EnumPreference<Spread>? = null,
    offsetFirstPage: Preference<Boolean>? = null,
    pageSpacing: RangePreference<Double>? = null
) {
    if (language != null || readingProgression != null) {
        if (language != null) {
            LanguageItem(
                preference = language,
                commit = commit
            )
        }

        if (readingProgression != null) {
            ButtonGroupItem(
                title = "Reading progression",
                preference = readingProgression,
                commit = commit,
                formatValue = { it.name }
            )
        }

        Divider()
    }

    if (scroll != null) {
        SwitchItem(
            title = "Scroll",
            preference = scroll,
            commit = commit
        )
    }

    if (scrollAxis != null) {
        ButtonGroupItem(
            title = "Scroll axis",
            preference = scrollAxis,
            commit = commit
        ) { value ->
            when (value) {
                Axis.HORIZONTAL -> "Horizontal"
                Axis.VERTICAL -> "Vertical"
            }
        }
    }

    if (spread != null) {
        ButtonGroupItem(
            title = "Spread",
            preference = spread,
            commit = commit
        ) { value ->
            when (value) {
                Spread.AUTO -> "Auto"
                Spread.NEVER -> "Never"
                Spread.ALWAYS -> "Always"
            }
        }

        if (offsetFirstPage != null) {
            SwitchItem(
                title = "Offset",
                preference = offsetFirstPage,
                commit = commit
            )
        }
    }

    if (fit != null) {
        ButtonGroupItem(
            title = "Fit",
            preference = fit,
            commit = commit
        ) { value ->
            when (value) {
                Fit.CONTAIN -> "Contain"
                Fit.COVER -> "Cover"
                Fit.WIDTH -> "Width"
                Fit.HEIGHT -> "Height"
            }
        }
    }

    if (pageSpacing != null) {
        StepperItem(
            title = "Page spacing",
            preference = pageSpacing,
            commit = commit
        )
    }
}

/**
 * User settings for a publication with adjustable fonts and dimensions, such as
 * a reflowable EPUB, HTML document or PDF with reflow mode enabled.
 */
@Composable
private fun ReflowableUserPreferences(
    commit: () -> Unit,
    backgroundColor: Preference<Color>? = null,
    columnCount: EnumPreference<ColumnCount>? = null,
    fontFamily: Preference<FontFamily?>? = null,
    fontSize: RangePreference<Double>? = null,
    fontWeight: RangePreference<Double>? = null,
    hyphens: Preference<Boolean>? = null,
    imageFilter: EnumPreference<ImageFilter?>? = null,
    language: Preference<Language?>? = null,
    letterSpacing: RangePreference<Double>? = null,
    ligatures: Preference<Boolean>? = null,
    lineHeight: RangePreference<Double>? = null,
    pageMargins: RangePreference<Double>? = null,
    paragraphIndent: RangePreference<Double>? = null,
    paragraphSpacing: RangePreference<Double>? = null,
    publisherStyles: Preference<Boolean>? = null,
    readingProgression: EnumPreference<ReadingProgression>? = null,
    scroll: Preference<Boolean>? = null,
    textAlign: EnumPreference<ReadiumTextAlign?>? = null,
    textColor: Preference<Color>? = null,
    textNormalization: Preference<Boolean>? = null,
    theme: EnumPreference<Theme>? = null,
    typeScale: RangePreference<Double>? = null,
    verticalText: Preference<Boolean>? = null,
    wordSpacing: RangePreference<Double>? = null
) {
    if (verticalText != null) {
        SwitchItem(
            title = stringResource(R.string.vertical_text),
            preference = verticalText,
            commit = commit
        )
    }

    if (scroll != null || columnCount != null || pageMargins != null) {
        if (scroll != null) {
            SwitchItem(
                title = stringResource(R.string.scroll),
                preference = scroll,
                commit = commit
            )
        }

        if (pageMargins != null) {
            StepperItem(
                title = stringResource(R.string.page_margins),
                preference = pageMargins,
                commit = commit
            )
        }

        Divider()
    }

    if (theme != null || textColor != null || imageFilter != null) {
        if (theme != null) {
            ButtonGroupItem(
                title = stringResource(R.string.theme),
                preference = theme,
                commit = commit
            ) { value ->
                when (value) {
                    Theme.LIGHT -> "Light"
                    Theme.DARK -> "Dark"
                    Theme.SEPIA -> "Sepia"
                }
            }
        }

        Divider()
    }

    if (fontFamily != null || fontSize != null || textNormalization != null) {
        if (fontFamily != null) {
            MenuItem(
                title = stringResource(R.string.font),
                preference = fontFamily
                    .withSupportedValues(
                        null,
                        FontFamily.LITERATA,
                        FontFamily.SANS_SERIF,
                        FontFamily.IA_WRITER_DUOSPACE,
                        FontFamily.ACCESSIBLE_DFA,
                        FontFamily.OPEN_DYSLEXIC
                    ),
                commit = commit
            ) { value ->
                when (value) {
                    null -> "Original"
                    FontFamily.SANS_SERIF -> "Sans Serif"
                    else -> value.name
                }
            }
        }

        if (fontSize != null) {
            StepperItem(
                title = stringResource(R.string.font_size),
                preference = fontSize,
                commit = commit
            )
        }

        if (fontWeight != null) {
            StepperItem(
                title = stringResource(R.string.font_weight),
                preference = fontWeight,
                commit = commit
            )
        }

        if (textNormalization != null) {
            SwitchItem(
                title = stringResource(R.string.text_normalization),
                preference = textNormalization,
                commit = commit
            )
        }
    }
}

@Composable
private fun Divider() {
    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
}

@Composable
private fun PresetsMenuButton(
    presets: List<Preset>,
    clear: () -> Unit,
    commit: () -> Unit
) {
    if (presets.isEmpty()) return

    DropdownMenuButton(
        text = { Text("Presets") }
    ) { dismiss ->

        for (preset in presets) {
            DropdownMenuItem(
                text = { Text(preset.title) },
                onClick = {
                    dismiss()
                    clear()
                    preset.apply()
                    commit()
                }
            )
        }
    }
}

/**
 * A preset is a named group of settings applied together.
 */

/**
 * A preset is a named group of settings applied together.
 */
class Preset(
    val title: String,
    val apply: () -> Unit
)

/**
 * Returns the presets associated with the [Configurable.Settings] receiver.
 */
val <P : Configurable.Preferences<P>> PreferencesEditor<P>.presets: List<Preset> get() =
    when (this) {
        is EpubPreferencesEditor ->
            when (layout) {
                EpubLayout.FIXED -> emptyList()
                EpubLayout.REFLOWABLE -> listOf(
                    Preset("Increase legibility") {
                        wordSpacing.set(0.6)
                        fontSize.set(1.4)
                        fontWeight.set(2.0)
                    },
                    Preset("Document") {
                        scroll.set(true)
                    },
                    Preset("Ebook") {
                        scroll.set(false)
                    },
                    Preset("Manga") {
                        scroll.set(false)
                        readingProgression.set(ReadingProgression.RTL)
                    }
                )
            }
        else ->
            emptyList()
    }
