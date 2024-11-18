package com.example.bookdy

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import com.example.bookdy.domain.Bookshelf
import com.example.bookdy.domain.ImportError
import com.example.bookdy.utils.EventChannel

class MainViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val app =
        getApplication<com.example.bookdy.Application>()

    val channel: EventChannel<Event> =
        EventChannel(Channel(Channel.UNLIMITED), viewModelScope)

    init {
        app.bookshelf.channel.receiveAsFlow()
            .onEach { sendImportFeedback(it) }
            .launchIn(viewModelScope)
    }

    private fun sendImportFeedback(event: Bookshelf.Event) {
        when (event) {
            is Bookshelf.Event.ImportPublicationError -> {
                channel.send(Event.ImportPublicationError(event.error))
            }
            Bookshelf.Event.ImportPublicationSuccess -> {
                channel.send(Event.ImportPublicationSuccess)
            }
        }
    }

    sealed class Event {

        object ImportPublicationSuccess :
            Event()

        class ImportPublicationError(
            val error: ImportError
        ) : Event()
    }
}
