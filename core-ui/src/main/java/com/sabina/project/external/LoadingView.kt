package com.sabina.project.external

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import com.sabina.project.ui.R

class LoadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val progressBar = ProgressBar(context).apply {
        layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        )
        indeterminateTintList = ColorStateList.valueOf(
            ContextCompat.getColor(context, R.color.colorPrimary)
        )
    }

    init {
        setBackgroundColor(ContextCompat.getColor(context, R.color.colorOnBackground))
        alpha = 0.75f
        isClickable = true
        isFocusable = true
        translationZ = 16f
        addView(progressBar)
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        progressBar.isIndeterminate = visibility == View.VISIBLE
    }
}