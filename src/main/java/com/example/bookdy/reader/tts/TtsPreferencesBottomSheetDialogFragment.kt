package com.example.bookdy.reader.tts

import androidx.fragment.app.activityViewModels
import org.readium.r2.shared.ExperimentalReadiumApi
import com.example.bookdy.reader.ReaderViewModel
import com.example.bookdy.reader.preferences.UserPreferencesBottomSheetDialogFragment
import com.example.bookdy.reader.preferences.UserPreferencesViewModel

@OptIn(ExperimentalReadiumApi::class)
class TtsPreferencesBottomSheetDialogFragment : UserPreferencesBottomSheetDialogFragment(
    "Settings"
) {

    private val viewModel: ReaderViewModel by activityViewModels()

    override val preferencesModel: UserPreferencesViewModel<*, *> by lazy {
        checkNotNull(viewModel.tts!!.preferencesModel)
    }
}
