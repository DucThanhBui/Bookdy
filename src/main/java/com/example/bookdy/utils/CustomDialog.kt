package com.example.bookdy.utils

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.bookdy.R
import com.example.bookdy.bookshelf.BookshelfViewModel
import com.example.bookdy.data.model.Book
import com.example.bookdy.favorite.FavoriteFragment.Companion.ADD_TO_FAVORITE
import com.google.android.material.dialog.MaterialAlertDialogBuilder


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
        if (book.isFavorite == 1) favButton.visibility = View.GONE

        val deleteButton: TextView = view.findViewById(R.id.delete_book)

        val uploadButton: TextView = view.findViewById(R.id.upload)
        if (book.isSync == 0 && isLogin) uploadButton.visibility = View.VISIBLE

        favButton.setOnClickListener {
            bookshelfViewModel.markFavorite(book, ADD_TO_FAVORITE)
            dismiss()
        }

        deleteButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.confirm_delete_book_title))
            .setMessage(getString(R.string.confirm_delete_book_text))
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

        return view

    }

}