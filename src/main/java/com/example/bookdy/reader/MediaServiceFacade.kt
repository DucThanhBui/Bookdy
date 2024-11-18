package com.example.bookdy.reader

import android.app.Application
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.readium.navigator.media.common.Media3Adapter
import org.readium.r2.shared.ExperimentalReadiumApi
import com.example.bookdy.utils.CoroutineQueue

/**
 * Enables to try to close a session without starting the [MediaService] if it is not started.
 */
@OptIn(ExperimentalReadiumApi::class)
class MediaServiceFacade(
    private val application: Application
) {
    private val coroutineScope: CoroutineScope =
        MainScope()

    private val coroutineQueue: CoroutineQueue =
        CoroutineQueue()

    private var binder: MediaService.Binder? =
        null

    private var bindingJob: Job? =
        null

    private val sessionMutable: MutableStateFlow<MediaService.Session?> =
        MutableStateFlow(null)

    val session: StateFlow<MediaService.Session?> =
        sessionMutable.asStateFlow()

    /**
     * Throws an IllegalStateException if binding to the MediaService fails.
     */
    suspend fun <N> openSession(
        bookIdf: String,
        navigator: N
    ) where N : AnyMediaNavigator, N : Media3Adapter {
        coroutineQueue.await {
            MediaService.start(application)
            binder = try {
                MediaService.bind(application)
            } catch (e: Exception) {
                // Failed to bind to the service.
                MediaService.stop(application)
                throw e
            }

            bindingJob = binder!!.session
                .onEach { sessionMutable.value = it }
                .launchIn(coroutineScope)
            binder!!.openSession(navigator, bookIdf)
        }
    }

    fun closeSession() {
        coroutineQueue.launch {
            bindingJob?.cancelAndJoin()
            binder?.closeSession()
            binder?.stop()
            sessionMutable.value = null
            binder = null
        }
    }
}
