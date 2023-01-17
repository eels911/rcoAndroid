package com.sabina.project.external

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.text.Spanned
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.sabina.project.ui.R
import com.sabina.project.ui.databinding.PopupContentBinding
import timber.log.Timber
import java.lang.Exception

class AlmagestSnackbar private constructor(
    private val context: Context,
    private val status: Int,
    private val message: String,
    private val gravity: Int,
    private val delayMillis: Long,
    private val onDismiss: (() -> Unit)?
) {

    private val handler = Handler(Looper.getMainLooper())
    private var popupWindow: PopupWindow? = null
    private var snackbar: Snackbar? = null

    fun String.html(): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(this, Html.FROM_HTML_MODE_COMPACT)
        } else {
            Html.fromHtml(this)
        }
    }

    fun show(parent: View) {
        val containerView = View.inflate(context, R.layout.popup_container, null)
        val contentView = View.inflate(context, R.layout.popup_content, null)
        val contentBinding = PopupContentBinding.bind(contentView)

        val iconAndColor = when (status) {
            2 -> R.color.green to R.drawable.ic_success_24
            1 -> R.color.yellow to R.drawable.ic_warning_24
            else -> R.color.red to R.drawable.ic_error_24
        }
        contentBinding.clContentBorder.setBackgroundColor(ContextCompat.getColor(context, iconAndColor.first))
        contentBinding.ivIcon.setColorFilter(ContextCompat.getColor(context, iconAndColor.first))
        contentBinding.ivIcon.setImageResource(iconAndColor.second)
        contentBinding.tvMessage.text = message.html()

        popupWindow = PopupWindow(
            containerView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        ).apply { isClippingEnabled = false }

        handler.postDelayed({
            try {
                popupWindow?.showAtLocation(parent, gravity, 0, 250)
                snackbar = Snackbar.make(containerView, message, Snackbar.LENGTH_LONG).apply {
                    setBackgroundTint(ContextCompat.getColor(context, R.color.navigationBackground))
                    setCustomView(contentView)
                    addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            super.onDismissed(transientBottomBar, event)
                            this@AlmagestSnackbar.dismiss()
                        }
                    })
                    show()
                }
            } catch (e: Exception) {
                Timber.d(e.message + e.stackTrace)
            }
        }, delayMillis)
    }

    fun dismiss() {
        snackbar?.dismiss()
        popupWindow?.dismiss()
        onDismiss?.invoke()
    }

    private fun Snackbar.setCustomView(customView: View) {
        (view as Snackbar.SnackbarLayout).removeAllViews()
        (view as Snackbar.SnackbarLayout).addView(customView)
        (view as Snackbar.SnackbarLayout).setPadding(0, 0, 0, 0)
        customView.clipToOutline = true
        customView.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = context.resources.getDimension(
                R.dimen.radius_mini
            )
        }
    }

    class Builder(private val context: Context) {
        private var status: Int = 0
        private var message: String? = null
        private var messageId: Int? = null
        private var gravity: Int = Gravity.TOP
        private var delayMillis = 0L
        private var onDismiss: (() -> Unit)? = null

        fun setStatus(status: Int): Builder {
            this.status = status
            return this
        }

        fun setMessage(message: String?): Builder {
            this.message = message
            return this
        }

        fun setMessage(@StringRes messageId: Int?): Builder {
            this.messageId = messageId
            return this
        }

        fun setGravity(gravity: Int): Builder {
            this.gravity = gravity
            return this
        }

        fun setDelay(delayMillis: Long): Builder {
            this.delayMillis = delayMillis
            return this
        }

        fun setOnDismissCallback(onDismiss: (() -> Unit)?): Builder {
            this.onDismiss = onDismiss
            return this
        }

        fun build() = AlmagestSnackbar(
            context = context,
            status = status,
            message = message ?: messageId?.let { context.getString(it) } ?: "",
            gravity = gravity,
            delayMillis = delayMillis,
            onDismiss = onDismiss
        )
    }
}