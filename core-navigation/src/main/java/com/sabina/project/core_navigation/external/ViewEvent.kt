package com.sabina.project.core_navigation.external

import android.view.Gravity
import androidx.annotation.StringRes
import com.sabina.project.base.external.models.NotificationStatus

sealed class ViewEvent {
    class AlmagestSnackbar(
        val message: String? = null,
        @StringRes val messageId: Int? = null,
        val status: NotificationStatus,
        val delayMillis: Long = 0L,
        val gravity: Int = Gravity.TOP,
        val onDismiss: (() -> Unit)? = null
    ) : ViewEvent()

    class Navigation(val screen: Screen) : ViewEvent()

    sealed class Loading : ViewEvent() {
        object Enabled : Loading()
        object Disabled : Loading()
    }
}