package com.example.bookdy.utils.extensions

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import kotlin.time.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*


@Composable
fun <T> StateFlow<T>.asStateWhenStarted(): State<T> =
    asStateWhenStarted(transform = { it })

@Composable
@Suppress("StateFlowValueCalledInComposition")
fun <T, R> StateFlow<T>.asStateWhenStarted(transform: (T) -> R): State<R> {
    val owner = LocalLifecycleOwner.current
    return remember(this, owner) {
        map(transform)
            .flowWithLifecycle(owner.lifecycle)
    }.collectAsState(initial = transform(value))
}

fun <T> Flow<T>.throttleLatest(period: Duration): Flow<T> =
    flow {
        conflate().collect {
            emit(it)
            delay(period)
        }
    }

suspend fun <P> Flow<P>.stateInFirst(scope: CoroutineScope, sharingStarted: SharingStarted) =
    stateIn(scope, sharingStarted, first())

fun <T, M> StateFlow<T>.mapStateIn(
    coroutineScope: CoroutineScope,
    transform: (value: T) -> M
): StateFlow<M> =
    map { transform(it) }
        .stateIn(
            coroutineScope,
            SharingStarted.Eagerly,
            transform(value)
        )
suspend fun <T> Flow<List<T>>.flattenToList(s: String) =
    flatMapConcat {
        Log.d("Readiumxxx", "$s flattenToList")
        it.asFlow()
    }.toList()