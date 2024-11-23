package com.example.bookdy.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.bookdy.R
import com.example.bookdy.bookshelf.BookshelfAdapter
import com.example.bookdy.bookshelf.BookshelfFragment.VerticalSpaceItemDecoration
import com.example.bookdy.bookshelf.BookshelfViewModel
import com.example.bookdy.data.model.Book
import com.example.bookdy.databinding.FragmentFavoriteBinding
import com.example.bookdy.utils.GridAutoFitLayoutManager
import com.example.bookdy.utils.viewLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FavoriteFragment : Fragment() {
    private val bookshelfViewModel: BookshelfViewModel by activityViewModels()
    private lateinit var bookshelfAdapter: BookshelfAdapter
    private lateinit var appStoragePickerLauncher: ActivityResultLauncher<String>
    private var binding: FragmentFavoriteBinding by viewLifecycle()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bookshelfAdapter = BookshelfAdapter(
            onBookClick = { book ->
                book.identifier.let {
                    bookshelfViewModel.openPublication(it)
                }
            },
            onBookLongClick = { book -> confirmDeleteFavoriteBook(book) }
        )

        binding.bookshelfFavorite.apply {
            setHasFixedSize(true)
            layoutManager = GridAutoFitLayoutManager(requireContext(), 120)
            adapter = bookshelfAdapter
            addItemDecoration(
                VerticalSpaceItemDecoration(
                    10
                )
            )
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                bookshelfViewModel.favoriteBooks.collectLatest {
                    bookshelfAdapter.submitList(it)
                }
            }
        }

    }

    private fun confirmDeleteFavoriteBook(book: Book) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.confirm_delete_fav_book_title))
            .setMessage(getString(R.string.confirm_delete_fav_book_text))
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton(getString(R.string.delete)) { dialog, _ ->
                bookshelfViewModel.markFavorite(book, REMOVE_FROM_FAVORITE)
                dialog.dismiss()
            }
            .show()
    }

    companion object {
        const val ADD_TO_FAVORITE = 1
        const val REMOVE_FROM_FAVORITE = 0
    }
}
