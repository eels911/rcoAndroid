package com.sabina.project.core_navigation.external.helpers

import android.os.Bundle
import com.sabina.project.core_navigation.external.ViewEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow

class MutableResultFlow(
    onResult: (String, Bundle) -> Unit,
) : ResultFlow(onResult), MutableSharedFlow<ViewEvent> {

    private val actualFlow = MutableSharedFlow<ViewEvent>()

    override val subscriptionCount: StateFlow<Int>
        get() = actualFlow.subscriptionCount
    override val replayCache: List<ViewEvent>
        get() = actualFlow.replayCache

    override suspend fun emit(value: ViewEvent) {
        if (value is ViewEvent.Navigation) {
            if (!value.screen.requestKey.isNullOrEmpty()) {
                pendingExecution = PendingExecution(
                    requestKey = value.screen.requestKey,
                )
            }
        }
        actualFlow.emit(value)
    }

    @InternalCoroutinesApi
    override suspend fun collect(collector: FlowCollector<ViewEvent>) {
        actualFlow.collect(collector)
    }

    @ExperimentalCoroutinesApi
    override fun resetReplayCache() {
        actualFlow.resetReplayCache()
    }

    override fun tryEmit(value: ViewEvent): Boolean {
        return actualFlow.tryEmit(value)
    }
}