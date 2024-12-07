package com.example.bookdy.utils

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.bookdy.R
import com.example.bookdy.bookshelf.BookshelfViewModel
import com.example.bookdy.data.model.Book
import com.example.bookdy.data.model.Bookmark
import com.example.bookdy.data.model.Highlight
import com.example.bookdy.data.model.HighlightConverters
import com.example.bookdy.data.modeljson.BookJson
import com.example.bookdy.data.modeljson.BookmarkJson
import com.example.bookdy.data.modeljson.HighlightJson
import com.example.bookdy.favorite.FavoriteFragment.Companion.ADD_TO_FAVORITE
import com.example.bookdy.utils.extensions.flattenToList
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.divider.MaterialDivider
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class CustomDialog(
    private val bookshelfViewModel: BookshelfViewModel,
    private val book: Book
) : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_bookshelf, container, false)
        dialog?.setTitle(context?.resources?.getString(R.string.dialog_title))
        val favButton: TextView = view.findViewById(R.id.add_book_fab)
        val dvdFav: MaterialDivider = view.findViewById(R.id.dvdAddFav)
        if (book.isFavorite == 1) {
            favButton.visibility = View.GONE
            dvdFav.visibility = View.GONE
        }

        val deleteButton: TextView = view.findViewById(R.id.delete_book)

        val uploadButton: TextView = view.findViewById(R.id.upload)
        val dividerUpload: MaterialDivider = view.findViewById(R.id.dvdUpload)
        Log.d("Readiumxxx", "book.sync is ${book.isSync}")
        if (isLogin) {
            uploadButton.visibility = View.VISIBLE
            dividerUpload.visibility = View.VISIBLE
        } else {
            uploadButton.visibility = View.GONE
            dividerUpload.visibility = View.GONE
        }

        favButton.setOnClickListener {
            bookshelfViewModel.markFavorite(book, ADD_TO_FAVORITE)
            Handler(Looper.getMainLooper()).post{
                Toast.makeText(requireContext(), R.string.added_to_favorite, Toast.LENGTH_SHORT).show()
            }
            dismiss()
        }

        deleteButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.confirm_delete_book_title))
            .setMessage(getString(R.string.confirm_delete_book_local_text))
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton(getString(R.string.delete)) { dialog, _ ->
                bookshelfViewModel.deletePublication(book)
                dialog.dismiss()
            }
            .show()
            dismiss()
        }

        uploadButton.setOnClickListener {
            if (bookshelfViewModel.networkStatus) {
                bookshelfViewModel.doUploadBook(book)
                bookshelfViewModel.markSync(book, BookshelfViewModel.SYNC_ED)
                dismiss()
            } else {
                bookshelfViewModel.showNetworkStatus()
                dismiss()
            }
        }

        return view

    }

}