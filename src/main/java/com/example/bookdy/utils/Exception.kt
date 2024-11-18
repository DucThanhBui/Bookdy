package com.example.bookdy.utils

import timber.log.Timber

inline fun <T> tryOrNull(closure: () -> T): T? =
    tryOr(null, closure)

inline fun <T> tryOr(default: T, closure: () -> T): T =
    try { closure() } catch (e: Exception) { default }

inline fun <T> tryOrLog(closure: () -> T): T? =
    try { closure() } catch (e: Exception) {
        Timber.e(e)
        null
    }
