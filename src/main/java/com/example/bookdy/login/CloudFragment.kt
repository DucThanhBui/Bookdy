package com.example.bookdy.login

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bookdy.Application
import com.example.bookdy.R
import com.example.bookdy.bookshelf.BookshelfFragment.VerticalSpaceItemDecoration
import com.example.bookdy.bookshelf.BookshelfViewModel
import com.example.bookdy.data.model.Book
import com.example.bookdy.data.model.Bookmark
import com.example.bookdy.data.model.Highlight
import com.example.bookdy.data.model.HighlightConverters
import com.example.bookdy.data.modeljson.BookJson
import com.example.bookdy.data.modeljson.BookJson.Companion.TAG
import com.example.bookdy.databinding.FragmentCloudBinding
import com.example.bookdy.databinding.ItemRecycleBookBinding
import com.example.bookdy.network.BookApiService
import com.example.bookdy.reader.ReaderActivityContract
import com.example.bookdy.utils.GridAutoFitLayoutManager
import com.example.bookdy.utils.singleClick
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.Url
import org.readium.r2.shared.util.mediatype.MediaType
import org.readium.r2.streamer.server.BASE_URL
import java.io.File
import kotlin.math.log

class CloudFragment : Fragment() {
    private var _binding : FragmentCloudBinding? = null
    private val binding get() = _binding!!
    private lateinit var cloudAdapter: CloudAdapter
    private val loginViewModel: LoginViewModel by activityViewModels()
    private val bookshelfViewModel: BookshelfViewModel by activityViewModels()


    private val app: Application
        get() = requireContext().applicationContext as Application

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCloudBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cloudAdapter = CloudAdapter(
            onBookClick = { book ->
                viewLifecycleOwner.lifecycleScope.launch {
                    if (app.bookRepository.get(book.identifier) != null) {
                        bookshelfViewModel.openPublicationDirectly(book.identifier, requireContext())
                    } else {
                        if (loginViewModel.networkStatus) {
                            val url = (BookApiService.BASE_URL + book.pathOnServer).replace("\\", "/")
                            app.bookshelf.importPublicationFromHttp(AbsoluteUrl.Companion.invoke(url)!!)
                            Log.e("Readiumxxx", "download book completed, start insert bookmarks")
                            val bookmarks = book.bookmarks
                            Log.e("Readiumxxx", "bookmarks size is ${bookmarks.size}")
                            bookmarks.forEach {
                                val bookmark = Bookmark(
                                    creation = it.creation,
                                    bookIdf = it.bookId,
                                    resourceIndex = it.resourceIndex,
                                    resourceHref = it.resourceHref,
                                    resourceType = it.resourceType,
                                    resourceTitle = it.resourceTitle,
                                    location = it.location,
                                    locatorText = it.locatorText
                                )
                                app.bookRepository.booksDao.insertBookmark(bookmark)
                            }
                            val highlights = book.highlights
                            val highlightConverters = HighlightConverters()
                            Log.e("Readiumxxx", "highlights size is ${highlights.size}")
                            highlights.forEach {
                                val locator = Locator(
                                    href = Url(it.href)!!,
                                    mediaType = MediaType(it.type) ?: MediaType.BINARY,
                                    title = it.title,
                                    locations = highlightConverters.locationsFromString(it.location),
                                    text = highlightConverters.textFromString(it.text)
                                )
                                val highlight = Highlight(
                                    bookIdf = it.bookId,
                                    tint = it.tint,
                                    locator = locator,
                                    annotation = it.annotation
                                )
                                app.bookRepository.booksDao.insertHighlight(highlight)
                            }
                        } else {
                            loginViewModel.showNetworkStatus(requireContext())
                        }
                    }
                }
            },
            onBookLongClick = { book -> confirmDeleteBook(book) },
            setIconDownloaded = { book: BookJson, imageView: ImageView ->
                viewLifecycleOwner.lifecycleScope.launch {
                    if (app.bookRepository.get(book.identifier) != null) {
                        Log.d(TAG, "xxxxxxxinside setIcon, book found")
                        imageView.visibility = View.VISIBLE
                    }
                }
            }
        )

        binding.cloudBookList.apply {
            setHasFixedSize(true)
            layoutManager = GridAutoFitLayoutManager(requireContext(), 120)
            adapter = cloudAdapter
            addItemDecoration(
                VerticalSpaceItemDecoration(
                    10
                )
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val books = loginViewModel.getAllBook()
            cloudAdapter.submitList(books)
        }
    }

    private fun confirmDeleteBook(book: BookJson) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.confirm_delete_book_title))
            .setMessage(getString(R.string.confirm_delete_book_text))
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton(getString(R.string.delete)) { dialog, _ ->
                deleteBook(book)
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteBook(bookJson: BookJson) {
        viewLifecycleOwner.lifecycleScope.launch {
            val book = app.bookRepository.get(bookJson.identifier)
            if (book != null) {
                Log.e("Readiumxxx", " book not null")
                bookshelfViewModel.deletePublication(book)
            }
            loginViewModel.deleteBook(bookJson, requireContext())
        }
    }
}

class CloudAdapter(
    private val setIconDownloaded: (BookJson, ImageView) -> Unit,
    private val onBookClick: (BookJson) -> Unit,
    private val onBookLongClick: (BookJson) -> Unit
) : ListAdapter<BookJson, CloudAdapter.ViewHolder>(BookListDiff()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            ItemRecycleBookBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val book = getItem(position)
        viewHolder.bind(book)
    }

    inner class ViewHolder(private val binding: ItemRecycleBookBinding) :
        RecyclerView.ViewHolder(binding.root) {

         fun bind(book: BookJson) {
            binding.bookshelfTitleText.text = book.filename
            setIconDownloaded(book, binding.iconDownloaded)
            //binding.iconDownloaded.visibility = View.VISIBLE
            val bookcoverUrl = (BookApiService.BASE_URL + book.cover).replace("\\", "/")
            Picasso.get()
                .load(bookcoverUrl)
                .placeholder(R.drawable.cover)
                .into(binding.bookshelfCoverImage)
            binding.root.singleClick {
                onBookClick(book)
            }
            binding.root.setOnLongClickListener {
                onBookLongClick(book)
                true
            }
        }
    }

    private class BookListDiff : DiffUtil.ItemCallback<BookJson>() {

        override fun areItemsTheSame(
            oldItem: BookJson,
            newItem: BookJson
        ): Boolean {
            return oldItem.identifier == newItem.identifier
        }

        override fun areContentsTheSame(
            oldItem: BookJson,
            newItem: BookJson
        ): Boolean {
            return oldItem.creation != newItem.creation
        }
    }
}