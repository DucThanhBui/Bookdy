package com.example.bookdy.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Creates a [ViewModelProvider.Factory] for a single type of [ViewModel] using the result of the
 * given [factory] closure.
 */
inline fun <reified T : ViewModel> createViewModelFactory(crossinline factory: () -> T): ViewModelProvider.Factory =

    object : ViewModelProvider.Factory {
        override fun <V : ViewModel> create(modelClass: Class<V>): V {
            if (!modelClass.isAssignableFrom(T::class.java)) {
                throw IllegalAccessException("Unknown ViewModel class")
            }
            @Suppress("UNCHECKED_CAST")
            return factory() as V
        }
    }
