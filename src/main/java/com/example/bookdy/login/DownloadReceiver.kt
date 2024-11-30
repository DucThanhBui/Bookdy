package com.example.bookdy.login

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.example.bookdy.Application
import com.example.bookdy.R
import com.example.bookdy.data.modeljson.BookJson.Companion.TAG
import com.example.bookdy.login.LoginViewModel.Companion.downloadId

class DownloadReceiver: BroadcastReceiver() {
    @SuppressLint("Range", "LogNotTimber")
    override fun onReceive(context: Context?, intent: Intent?) {
        // Get the download ID received with the broadcast
        Log.e(TAG, "onReceive ACTION_DOWNLOAD_COMPLETE")
        val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        Log.w(TAG, "id from intent: $id, downloadId = $downloadId")

        if (downloadId == id) {
            Log.e(TAG, "Download ID: $downloadId")
            val dm = context!!.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            val query = DownloadManager.Query()
            query.setFilterById(downloadId)
            val c = dm.query(query)
            if (c.moveToFirst()) {
                val colIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS)
                if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(colIndex)) {
                    Log.e(TAG, "Download Complete")
                    Toast.makeText(context, "Download Complete", Toast.LENGTH_SHORT).show()

                    val uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                    Log.i(TAG, "URI: $uriString")
                    val app = context.applicationContext as Application
                    app.bookshelf.importPublicationFromStorage(Uri.parse(uriString))
                } else {
                    Log.w(TAG, "Download Unsuccessful, Status Code: " + c.getInt(colIndex))
                    Toast.makeText(context, context.getString(R.string.download_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}