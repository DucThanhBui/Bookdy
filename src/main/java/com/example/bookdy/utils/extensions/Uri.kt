package com.example.bookdy.utils.extensions

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import java.io.File
import java.util.*
import org.readium.r2.shared.util.Try
import com.example.bookdy.utils.ContentResolverUtil
import com.example.bookdy.utils.tryOrNull

suspend fun Uri.copyToTempFile(context: Context, dir: File): Try<File, Exception> =
    try {
        val filename = UUID.randomUUID().toString()
        val file = File(dir, "$filename.${extension(context)}")
        ContentResolverUtil.getContentInputStream(context, this, file)
        Try.success(file)
    } catch (e: Exception) {
        Try.failure(e)
    }

private fun Uri.extension(context: Context): String? {
    if (scheme == ContentResolver.SCHEME_CONTENT) {
        tryOrNull {
            context.contentResolver.queryProjection(this, MediaStore.MediaColumns.DISPLAY_NAME)
                ?.let { filename ->
                    File(filename).extension
                        .takeUnless { it.isBlank() }
                }
        }?.let { return it }
    }

    return path?.let { File(it).extension }
}

private fun ContentResolver.queryProjection(uri: Uri, projection: String): String? =
    tryOrNull<String?> {
        query(uri, arrayOf(projection), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(0)
            }
            return null
        }
    }
