package com.example.bookdy.utils.extensions

import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.bookdy.R
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

@ColorInt
fun Context.color(@ColorRes id: Int): Int {
    return ContextCompat.getColor(this, id)
}

suspend fun Context.confirmDialog(
    message: String,
    @StringRes positiveButton: Int = R.string.ok,
    @StringRes negativeButton: Int = R.string.cancel
): Boolean =
    suspendCancellableCoroutine { cont ->
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton(getString(positiveButton)) { dialog, _ ->
                dialog.dismiss()
                cont.resume(true)
            }
            .setNegativeButton(getString(negativeButton)) { dialog, _ ->
                dialog.cancel()
            }
            .setOnCancelListener {
                cont.resume(false)
            }
            .show()
    }
