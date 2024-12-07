package com.example.bookdy.bookshelf

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
            //appStoragePickerLauncher.launch("application/epub+zip")
            //private fun pickFilesFromDevice() {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                type = "application/epub+zip"
            }
            startActivityForResult(intent, REQ_CODE_EPUB)
        }
    }


    override fun onActivityResult(
        requestCode: Int, resultCode: Int,
        resultData: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, resultData)

        if (requestCode == REQ_CODE_EPUB && resultCode == Activity.RESULT_OK) {
            // Check if multiple files were selected
            val clipData = resultData?.clipData
            if (clipData != null) {
                for (i in 0 until clipData.itemCount) {
                    val uri = clipData.getItemAt(i).uri
                    // Use the uri
                    Log.d("Readiumxxx", "Result URI multi " + uri)
                    app.bookshelf.importPublicationFromStorage(uri)
                }
            } else {
                // If only one file was selected, handle it here
                val uri = resultData?.data
                // Use the uri
                Log.e("Readiumxxx", "Result URI one " + uri)
                if (uri != null) app.bookshelf.importPublicationFromStorage(uri)
            }
        }
    }

    companion object {
        const val REQ_CODE_EPUB = 1
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
