package com.example.bookdy.bookshelf

import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.example.bookdy.Application
import com.example.bookdy.R
import com.example.bookdy.data.model.Book
import com.example.bookdy.databinding.FragmentBookshelfBinding
import com.example.bookdy.utils.GridAutoFitLayoutManager
import com.example.bookdy.reader.ReaderActivityContract
import com.example.bookdy.utils.CustomDialog
import com.example.bookdy.utils.NetworkListener
import com.example.bookdy.utils.viewLifecycle

class BookshelfFragment : Fragment() {

    private val bookshelfViewModel: BookshelfViewModel by activityViewModels()
    private lateinit var bookshelfAdapter: BookshelfAdapter
    private lateinit var appStoragePickerLauncher: ActivityResultLauncher<String>
    private var binding: FragmentBookshelfBinding by viewLifecycle()
    private val networkListener: NetworkListener by lazy { NetworkListener() }

    private val app: Application
        get() = requireContext().applicationContext as Application

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBookshelfBinding.inflate(inflater, container, false)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                networkListener.checkNetworkAvailability(requireContext()).collect { status ->
                    bookshelfViewModel.networkStatus = status
                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bookshelfViewModel.channel.receive(viewLifecycleOwner) { handleEvent(it) }

        bookshelfAdapter = BookshelfAdapter(
            onBookClick = { book ->
                book.identifier.let {
                    bookshelfViewModel.openPublication(it)
                }
            },
            onBookLongClick = { book -> showDialog(book) }
        )

        appStoragePickerLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    bookshelfViewModel.importPublicationFromStorage(it)
                }
            }

        binding.bookshelfBookList.apply {
            setHasFixedSize(true)
            layoutManager = GridAutoFitLayoutManager(requireContext(), 120)
            adapter = bookshelfAdapter
            addItemDecoration(
                VerticalSpaceItemDecoration(
                    10
                )
            )
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                bookshelfViewModel.books.collectLatest {
                    bookshelfAdapter.submitList(it)
                }
            }
        }

        binding.bookshelfAddBookFab.setOnClickListener {
            appStoragePickerLauncher.launch("application/epub+zip")
        }
    }

    private fun handleEvent(event: BookshelfViewModel.Event) {
        when (event) {
            is BookshelfViewModel.Event.OpenPublicationError -> {
                event.error.toUserError().show(requireActivity())
            }

            is BookshelfViewModel.Event.LaunchReader -> {
                val intent = ReaderActivityContract().createIntent(
                    requireContext(),
                    event.arguments
                )
                startActivity(intent)
            }
        }
    }

    class VerticalSpaceItemDecoration(private val verticalSpaceHeight: Int) :
        RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.bottom = verticalSpaceHeight
        }
    }

    private fun showDialog(book: Book) {
        CustomDialog(bookshelfViewModel, book).show(parentFragmentManager, "CustomDialog")
    }
}
