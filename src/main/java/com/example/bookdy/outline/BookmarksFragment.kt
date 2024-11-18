package com.example.bookdy.outline

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bookdy.R
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.readium.r2.shared.publication.Publication
import com.example.bookdy.data.model.Bookmark
import com.example.bookdy.databinding.FragmentListviewBinding
import com.example.bookdy.databinding.ItemRecycleBookmarkBinding
import com.example.bookdy.reader.ReaderViewModel
import com.example.bookdy.utils.viewLifecycle

class BookmarksFragment : Fragment() {

    lateinit var publication: Publication
    private lateinit var viewModel: ReaderViewModel
    private lateinit var bookmarkAdapter: BookmarkAdapter
    private var binding: FragmentListviewBinding by viewLifecycle()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ViewModelProvider(requireActivity())[ReaderViewModel::class.java].let {
            publication = it.publication
            viewModel = it
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bookmarkAdapter = BookmarkAdapter(
            requireContext(),
            onBookmarkDeleteRequested = { bookmark -> viewModel.deleteBookmark(bookmark.id!!) },
            onBookmarkSelectedRequested = { bookmark -> onBookmarkSelected(bookmark) }
        )
        binding.listView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = bookmarkAdapter
        }

        val comparator: Comparator<Bookmark> = compareBy(
            { it.resourceIndex },
            { it.locator.locations.progression }
        )
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getBookmarks().collectLatest {
                    val bookmarks = it.sortedWith(comparator)
                    bookmarkAdapter.submitList(bookmarks)
                }
            }
        }
    }

    private fun onBookmarkSelected(bookmark: Bookmark) {
        setFragmentResult(
            OutlineContract.REQUEST_KEY,
            OutlineContract.createResult(bookmark.locator)
        )
    }
}

class BookmarkAdapter(
    private val context: Context,
    private val onBookmarkDeleteRequested: (Bookmark) -> Unit,
    private val onBookmarkSelectedRequested: (Bookmark) -> Unit
) :
    ListAdapter<Bookmark, BookmarkAdapter.ViewHolder>(BookmarksDiff()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            ItemRecycleBookmarkBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class ViewHolder(val binding: ItemRecycleBookmarkBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {

        fun bind(bookmark: Bookmark) {
            val title = bookmark.resourceTitle.ifEmpty { context.resources.getString(R.string.no_title) }

            binding.bookmarkChapter.text = title
            bookmark.locator.locations.progression?.let { progression ->
                val formattedProgression = context.resources.getString(R.string.bookmark_progression) + ": ${(progression * 100).roundToInt()}%"
                binding.bookmarkProgression.text = formattedProgression
            }

            binding.bookmarkTimestamp.text = bookmark.creation

            binding.overflow.setOnClickListener {
                val popupMenu = PopupMenu(binding.overflow.context, binding.overflow)
                popupMenu.menuInflater.inflate(R.menu.menu_bookmark, popupMenu.menu)
                popupMenu.show()

                popupMenu.setOnMenuItemClickListener { item ->
                    if (item.itemId == R.id.delete) {
                        onBookmarkDeleteRequested(bookmark)
                    }
                    false
                }
            }

            binding.root.setOnClickListener {
                onBookmarkSelectedRequested(bookmark)
            }
        }
    }
}

private class BookmarksDiff : DiffUtil.ItemCallback<Bookmark>() {

    override fun areItemsTheSame(
        oldItem: Bookmark,
        newItem: Bookmark
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: Bookmark,
        newItem: Bookmark
    ): Boolean {
        return oldItem.bookIdf == newItem.bookIdf &&
            oldItem.location == newItem.location
    }
}
