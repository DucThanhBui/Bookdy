package com.example.bookdy.reader

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.bookdy.R
import org.readium.r2.navigator.Navigator
import org.readium.r2.navigator.preferences.Configurable
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import com.example.bookdy.reader.preferences.MainPreferencesBottomSheetDialogFragment
import com.example.bookdy.utils.NetworkListener
import com.example.bookdy.utils.UserError
import com.example.bookdy.utils.isLogin
import kotlinx.coroutines.launch

abstract class BaseReaderFragment : Fragment() {

    val readerViewModel: ReaderViewModel by activityViewModels()
    protected val publication: Publication get() = readerViewModel.publication

    private val networkListener: NetworkListener by lazy { NetworkListener() }

    protected abstract val navigator: Navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        readerViewModel.fragmentChannel.receive(this) { event ->
            fun toast(id: Int) {
                Toast.makeText(requireContext(), getString(id), Toast.LENGTH_SHORT).show()
            }

            when (event) {
                is ReaderViewModel.FragmentFeedback.BookmarkFailed -> toast(
                    R.string.bookmark_exists
                )
                is ReaderViewModel.FragmentFeedback.BookmarkSuccessfullyAdded -> toast(
                    R.string.bookmark_added
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                networkListener.checkNetworkAvailability(requireContext()).collect { status ->
                    readerViewModel.networkStatus = status
                }
            }
        }

        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_reader, menu)

                    menu.findItem(R.id.settings).isVisible =
                        navigator is Configurable<*, *>
                    menu.findItem(R.id.summarize).isVisible = isLogin
                    menu.findItem(R.id.sync).isVisible = isLogin
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    when (menuItem.itemId) {
                        R.id.toc -> {
                            readerViewModel.activityChannel.send(
                                ReaderViewModel.ActivityCommand.OpenOutlineRequested
                            )
                            return true
                        }
                        R.id.bookmark -> {
                            readerViewModel.insertBookmark(navigator.currentLocator.value)
                            return true
                        }
                        R.id.settings -> {
                            MainPreferencesBottomSheetDialogFragment()
                                .show(childFragmentManager, "Settings")
                            return true
                        }
                    }
                    return false
                }
            },
            viewLifecycleOwner
        )
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        setMenuVisibility(!hidden)
        requireActivity().invalidateOptionsMenu()
    }

    open fun go(locator: Locator, animated: Boolean) {
        navigator.go(locator, animated)
    }

    protected fun showError(error: UserError) {
        val activity = activity ?: return
        error.show(activity)
    }
}
