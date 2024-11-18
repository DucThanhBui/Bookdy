package com.example.bookdy.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.content.IntentCompat
import com.example.bookdy.Application
import com.example.bookdy.MainActivity
import timber.log.Timber

class ImportActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        importPublication(intent)

        val newIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(newIntent)

        finish()
    }

    private fun importPublication(intent: Intent) {
        val uri = uriFromIntent(intent)
            ?: run {
                Timber.d("Got an empty intent.")
                return
            }

        val app = application as Application
        app.bookshelf.importPublicationFromStorage(uri)
    }

    private fun uriFromIntent(intent: Intent): Uri? =
        when (intent.action) {
            Intent.ACTION_SEND -> {
                if ("text/plain" == intent.type) {
                    intent.getStringExtra(Intent.EXTRA_TEXT).let { Uri.parse(it) }
                } else {
                    IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
                }
            }
            else -> {
                intent.data
            }
        }
}
