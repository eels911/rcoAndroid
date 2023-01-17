package com.sabina.project.core_navigation.external.helpers

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.sabina.project.core_navigation.external.ViewEvent
import kotlinx.coroutines.flow.SharedFlow

abstract class ResultFlow(
    private val onResult: (String, Bundle) -> Unit
) : SharedFlow<ViewEvent> {

    val isPendingExecution: Boolean
        get() = pendingExecution != null

    protected var pendingExecution: PendingExecution? = null

    fun setResultListener(fragment: Fragment) {
        val requestKey = pendingExecution?.requestKey ?: return
        fragment.navHostFragmentManager()
            .setFragmentResultListener(requestKey, fragment) { resultKey, data ->
                if (resultKey == requestKey) {
                    /**
                     * В момент вызова [onResult], переменная [pendingExecution] должна быть null
                     * для избежания проблем с навигационными ивентами.
                     */
                    val tmp = pendingExecution
                    pendingExecution = null
                    onResult(resultKey, data)
                }
            }
    }

    protected class PendingExecution(
        val requestKey: String? = null,
    )
}