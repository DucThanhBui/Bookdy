package com.example.bookdy.reader

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.os.bundleOf

class ReaderActivityContract :
    ActivityResultContract<ReaderActivityContract.Arguments, ReaderActivityContract.Arguments?>() {

    data class Arguments(val bookIdf: String)

    override fun createIntent(context: Context, input: Arguments): Intent {
        val intent = Intent(context, ReaderActivity::class.java)
        val arguments = bundleOf("bookIdf" to input.bookIdf)
        intent.putExtras(arguments)
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Arguments? {
        if (intent == null) {
            return null
        }

        val extras = requireNotNull(intent.extras)
        return parseExtras(extras)
    }

    companion object {

        fun parseIntent(activity: Activity): Arguments {
            val extras = requireNotNull(activity.intent.extras)
            return parseExtras(extras)
        }

        private fun parseExtras(extras: Bundle): Arguments {
            val bookIdf = extras.getString("bookIdf")
            check(bookIdf != null && bookIdf != "")
            return Arguments(bookIdf)
        }
    }
}
