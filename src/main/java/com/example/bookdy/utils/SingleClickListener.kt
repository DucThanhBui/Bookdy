package com.example.bookdy.utils

import android.view.View

class SingleClickListener(private val click: (v: View) -> Unit) : View.OnClickListener {

    companion object {
        private const val DOUBLE_CLICK_TIMEOUT = 2500
    }

    private var lastClick: Long = 0

    override fun onClick(v: View) {
        if (getLastClickTimeout() > DOUBLE_CLICK_TIMEOUT) {
            lastClick = System.currentTimeMillis()
            click(v)
        }
    }

    private fun getLastClickTimeout(): Long {
        return System.currentTimeMillis() - lastClick
    }
}

fun View.singleClick(l: (View) -> Unit) {
    setOnClickListener(SingleClickListener(l))
}
