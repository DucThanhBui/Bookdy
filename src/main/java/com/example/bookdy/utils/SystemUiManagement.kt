package com.example.bookdy.utils

import android.app.Activity
import android.view.View
import android.view.WindowInsets
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat

@Suppress("DEPRECATION")
private fun Activity.isSystemUiVisible(): Boolean {
    return this.window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0
}

@Suppress("DEPRECATION")
fun Activity.hideSystemUi() {
    this.window.decorView.systemUiVisibility = (
        View.SYSTEM_UI_FLAG_IMMERSIVE
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN
        )
}

@Suppress("DEPRECATION")
fun Activity.showSystemUi() {
    this.window.decorView.systemUiVisibility = (
        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        )
}

fun Activity.toggleSystemUi() {
    if (this.isSystemUiVisible()) {
        this.hideSystemUi()
    } else {
        this.showSystemUi()
    }
}

fun View.padSystemUi(insets: WindowInsets, activity: AppCompatActivity) =
    WindowInsetsCompat.toWindowInsetsCompat(insets, this)
        .getInsetsIgnoringVisibility(WindowInsetsCompat.Type.systemBars()).apply {
            setPadding(
                left,
                top + activity.supportActionBar!!.height,
                right,
                bottom
            )
        }

fun View.clearPadding() =
    setPadding(0, 0, 0, 0)
