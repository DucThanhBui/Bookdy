package com.example.bookdy.utils

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class CoroutineQueue(
    dispatcher: CoroutineDispatcher = Dispatchers.Main
) {
    private val scope: CoroutineScope =
        CoroutineScope(dispatcher + SupervisorJob())

    private val tasks: Channel<Task<*>> = Channel(Channel.UNLIMITED)

    init {
        scope.launch {
            for (task in tasks) {
                // Don't fail the root job if one task fails.
                supervisorScope {
                    task()
                }
            }
        }
    }


    fun launch(block: suspend () -> Unit) {
        tasks.trySendBlocking(Task(block)).getOrThrow()
    }

    fun <T> async(block: suspend () -> T): Deferred<T> {
        val deferred = CompletableDeferred<T>()
        val task = Task(block, deferred)
        tasks.trySendBlocking(task).getOrThrow()
        return deferred
    }

    suspend fun <T> await(block: suspend () -> T): T =
        async(block).await()

    private class Task<T>(
        val task: suspend () -> T,
        val deferred: CompletableDeferred<T>? = null
    ) {
        suspend operator fun invoke() {
            try {
                val result = task()
                deferred?.complete(result)
            } catch (e: Exception) {
                deferred?.completeExceptionally(e)
            }
        }
    }
}
