package com.example.bookdy.login

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import com.example.bookdy.R
import com.example.bookdy.network.BookApiService
import timber.log.Timber

class LoginViewModel(application: Application): AndroidViewModel(application) {

    var token: String? = null
    var networkStatus = false

    suspend fun loginUser(username: String, password: String): Boolean {
        val response = BookApiService.retrofitService.login(username, password)
        if (response.status == 0) {
            token = "Bearer ${response.token}"
            Timber.tag("Readiumxxx").d("token response %s", response.token)
            Timber.tag("Readiumxxx").d("token set to %s", token)
            return true
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
}