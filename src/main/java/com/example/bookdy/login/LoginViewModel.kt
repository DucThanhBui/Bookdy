package com.example.bookdy.login

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookdy.R
import com.example.bookdy.data.modeljson.BookJson
import com.example.bookdy.data.modeljson.BookJson.Companion.TAG
import com.example.bookdy.network.BookApiService
import com.example.bookdy.network.BookApiService.BASE_URL
import com.example.bookdy.utils.global_token
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.SocketTimeoutException

class LoginViewModel(application: Application): AndroidViewModel(application) {

    var token: String? = null
    var networkStatus = false

    suspend fun getAllBook() = BookApiService.retrofitService.getAllFiles(token!!)

    fun downloadBook(book: BookJson, context: Context) {
        val url = (BookApiService.BASE_URL + book.pathOnServer).replace("\\", "/")
        Log.e("Readiumxxx","url is $url")
        Log.e("Readiumxxx","uri is ${Uri.parse(url)}")
        val fileName = url.substring(url.lastIndexOf('/') + 1)
        Log.e("Readiumxxx","fileName is $fileName")

        Log.e("Readiumxxx", "mime type is ${getMimeFromFileName(fileName)}")

        val request = DownloadManager.Request(Uri.parse(url))
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
            .setTitle("${context.getString(R.string.downloading)}: ${book.filename}.epub")
            .setVisibleInDownloadsUi(true)
            .setMimeType(getMimeFromFileName(fileName));

        val dm = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        Toast.makeText(context, context.getString(R.string.starting_download_book), Toast.LENGTH_SHORT).show()
        downloadId = dm.enqueue(request)
        Log.e("Readiumxxx", "Download ID: $downloadId")
    }

    private fun getMimeFromFileName(fileName: String): String? {
        val map = MimeTypeMap.getSingleton()
        val ext = MimeTypeMap.getFileExtensionFromUrl(fileName)
        return map.getMimeTypeFromExtension(ext)
    }

    fun deleteBook(book: BookJson, context: Context) {
        if (networkStatus && !token.isNullOrEmpty()) {
            viewModelScope.launch {
                val response = BookApiService.retrofitService.deleteBook(token!!, book.identifier)
                if (response.status == -1) {
                    Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, context.getString(R.string.delete_complete), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    suspend fun loginUser(username: String, password: String): Boolean {
        try {
            val response = BookApiService.retrofitService.login(username, password)
            if (response.status == 0) {
                token = "Bearer ${response.token}"
                global_token = token as String
                //Log.e("Readiumxxx","token response ${response.token}")
                //Log.e("Readiumxxx","token set to $token")
                return true
            }
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "SocketTimeoutException:${e.message}")
        }

        return false
    }

    fun showNetworkStatus(context: Context) {
        if (!networkStatus) {
            Toast.makeText(getApplication(), context.getString(R.string.no_internet), Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun doChangePassword(currentPwd: String, newPwd: String): Boolean {
        Timber.tag("Readiumxxx").d("current token is %s", token)
        if (token == null) {
            Toast.makeText(getApplication(), "Please login first", Toast.LENGTH_SHORT).show()
            return false
        }
        val response = BookApiService.retrofitService.changePassword(currentPwd, newPwd, token!!)
        return response.status == 0

    }

    companion object {
        var downloadId = -1L
    }
}