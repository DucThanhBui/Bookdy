package com.example.bookdy.utils.extensions

import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun File.moveTo(target: File) = withContext(Dispatchers.IO) {
    if (this@moveTo.renameTo(target)) {
        return@withContext
    }
    copyTo(target)
    delete()
}
