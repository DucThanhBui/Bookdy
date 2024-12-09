package com.example.bookdy.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.bookdy.R
import com.example.bookdy.network.BookApiService
import com.example.bookdy.reader.ReaderViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.util.Locale


class CustomTranslateDialog(
    private val src: String,
    private val des: String,
    private val text: String?,
    private val before: String,
    private val after: String,
    private val readerViewModel: ReaderViewModel,
    private val scope: CoroutineScope
): DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_translate, container, false)
        val tvSourceLang = view.findViewById<TextView>(R.id.tvSourceLang)
        val tvDesLang = view.findViewById<TextView>(R.id.tvDesLang)
        tvSourceLang.text = src
        tvDesLang.text = des

        val tvSrcTitle = view.findViewById<TextView>(R.id.tvSrcTitle)
        val tvDesTitle = view.findViewById<TextView>(R.id.tvDesTitle)
        tvSrcTitle.text = src
        tvDesTitle.text = des

        val listAllLanguage = mutableListOf<String>()
        Locale.getAvailableLocales().forEach {
            listAllLanguage.add(it.displayName)
        }

        tvDesLang.setOnClickListener {
            val b = AlertDialog.Builder(requireContext())
            b.setTitle("Example")
            val types = listAllLanguage.toTypedArray()
            b.setItems(types) { dialog, which ->
                dialog.dismiss()
                val selected = types[which]
                tvDesLang.text = selected
                tvDesTitle.text = selected
            }
            b.show()
        }

        tvSourceLang.setOnClickListener {
            val b = AlertDialog.Builder(requireContext())
            b.setTitle("Example")
            val types = listAllLanguage.apply { add(0, requireContext().getString(R.string.auto_detect)) }.toTypedArray()
            b.setItems(types) { dialog, which ->
                dialog.dismiss()
                val selected = types[which]
                tvSourceLang.text = selected
                tvSrcTitle.text = selected
            }
            b.show()
        }

        val sourceText = view.findViewById<TextView>(R.id.tvSrc)
        sourceText.text = text
        val desText = view.findViewById<TextView>(R.id.tvDes)
        doTranslate(sourceText.text as String?, before, after, src, des, desText)

        val btSwitch = view.findViewById<FrameLayout>(R.id.flSwitchLang)
        btSwitch.setOnClickListener {
            val tmp = tvSourceLang.text
            tvSourceLang.text = tvDesLang.text
            tvDesLang.text = tmp

            val tmp2 = tvSrcTitle.text
            tvSrcTitle.text = tvDesTitle.text
            tvDesTitle.text = tmp2

            sourceText.text = desText.text
            //doTranslate(sourceText.text as String?, before, after, src, des, desText)
        }

        val btCopy = view.findViewById<Button>(R.id.copyButton)
        btCopy.setOnClickListener {
            val clipboard =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clipData = android.content.ClipData.newPlainText("translate", desText.text)
            clipboard.setPrimaryClip(clipData)
        }

        val btTranslate = view.findViewById<Button>(R.id.btTranslate)

        btTranslate.setOnClickListener {
            doTranslate(sourceText.text as String?, before, after,
                tvSourceLang.text as String, tvDesLang.text as String, desText)
        }

        dialog?.setCancelable(true)


        return view
    }

    private fun doTranslate(s: String?, before: String, after: String, src: String, dest: String, textView: TextView) {
        if (readerViewModel.networkStatus) {
            scope.launch {
                try {
                    val response = BookApiService.retrofitService.getTranslate(s ?: "", before, after, src, dest)
                    //Handler(Looper.getMainLooper()).post {
                        if (response.status == 0) {
                            textView.text = response.message
                        }
                    //}
                } catch (e: Exception) {
                    if (e is HttpException) {
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(requireContext(), R.string.server_error, Toast.LENGTH_LONG).show()
                        }
                    }
                    Log.e("Readiumxxx", e.message.toString())
                }
            }
        }
    }
}