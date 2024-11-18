package com.example.bookdy.shared.views

import androidx.compose.runtime.Composable
import com.example.bookdy.utils.compose.Emphasis
import com.example.bookdy.utils.compose.EmphasisProvider
import com.example.bookdy.utils.compose.LocalContentEmphasis

@Composable
fun Group(enabled: Boolean = true, content: @Composable () -> Unit) {
    val emphasis = when {
        !enabled -> Emphasis.Disabled
        else -> Emphasis.Medium
    }
    EmphasisProvider(LocalContentEmphasis provides emphasis) {
        content()
    }
}
