package com.sabina.project.core_navigation.external.helpers

import android.graphics.Point
import android.os.Bundle
import android.view.*
import androidx.annotation.LayoutRes
import androidx.core.content.getSystemService
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.sabina.project.base.R
import com.sabina.project.base.external.extensions.dpToPx
import com.sabina.project.base.external.ui.KeyboardManager
import kotlin.math.roundToInt

abstract class BottomSheetFragment(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId) {

    private val bottomSheetSlideCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) = Unit
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            onBottomSheetSlide(bottomSheet, slideOffset)
        }
    }
    private val bottomSheetStateCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            onBottomSheetStateChanged(bottomSheet, newState)
        }
    }

    private var containerLayout: View? = null
    private var contentLayout: View? = null
    private var bottomSheet: ViewGroup? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        containerLayout = inflater.inflate(R.layout.layout_bottom_sheet, container, false)
        contentLayout = super.onCreateView(inflater, container, savedInstanceState)
        bottomSheet = containerLayout?.findViewById(R.id.cvBtmSheet)

        containerLayout?.setOnClickListener {
            collapseInternalImpl()
        }
        contentLayout?.updatePadding(top = requireContext().dpToPx(16))
        contentLayout?.updateLayoutParams {
            height = (calculateScreenHeight() * calculateScreenOffset()).roundToInt()
        }
        bottomSheet?.updateLayoutParams {
            height = (calculateScreenHeight() * calculateScreenOffset()).roundToInt()
        }
        return containerLayout?.also {
            bottomSheet?.addView(contentLayout)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomSheet?.let { bottomSheet ->
            val behavior = createBottomSheetBehavior(bottomSheet)
            behavior.addBottomSheetCallback(bottomSheetSlideCallback)
            behavior.addBottomSheetCallback(bottomSheetStateCallback)
            behavior.skipCollapsed = true
            behavior.isHideable = true
        }
        view.post(::expandInternalImpl)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bottomSheet = null
        containerLayout = null
        contentLayout = null
    }

    override fun onDestroy() {
        super.onDestroy()
        (requireActivity() as? KeyboardManager)?.hideKeyboard()
    }

    /**
     * Всегда возвращает актуальное смещение bottom sheet, был-ли это вызов expand/collapse или же
     * пользователь сам смахнул его пальцем.
     */
    open fun onBottomSheetSlide(bottomSheet: View, slideOffset: Float) = Unit

    /**
     * Метод будет вызван ТОЛЬКО при закрытии bottom sheet жестом (пальцем).
     * Методы expand/collapse не затронут данный коллбек.
     */
    open fun onBottomSheetStateChanged(bottomSheet: View, newState: Int) = Unit

    /**
     * Если по какой-то причине понадобится поведение отличное от стандартного,
     * переопределите этот метод и добавьте нужные параметры в BottomSheetBehavior.
     */
    open fun createBottomSheetBehavior(bottomSheet: View) = BottomSheetBehavior.from(bottomSheet)

    /**
     * Вычисление доступной высоты экрана.
     */
    open fun calculateScreenHeight(): Int {
        val point = Point()
        requireContext().getSystemService<WindowManager>()!!
            .defaultDisplay.getSize(point)
        return point.y
    }

    /**
     * Максимальная раскрываемая высота bottom sheet.
     */
    open fun calculateScreenOffset(): Float = 0.9f

    fun expand(block: () -> Unit = {}) {
        bottomSheet?.let { bottomSheet ->
            val behavior = createBottomSheetBehavior(bottomSheet)
            behavior.removeBottomSheetCallback(bottomSheetStateCallback)
            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                        behavior.addBottomSheetCallback(bottomSheetStateCallback)
                        behavior.removeBottomSheetCallback(this)
                        block.invoke()
                    }
                }
            })
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    fun collapse(block: () -> Unit = {}) {
        bottomSheet?.let { bottomSheet ->
            val behavior = createBottomSheetBehavior(bottomSheet)
            behavior.removeBottomSheetCallback(bottomSheetStateCallback)
            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        behavior.addBottomSheetCallback(bottomSheetStateCallback)
                        behavior.removeBottomSheetCallback(this)
                        block.invoke()
                    }
                }
            })
            behavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    fun requireContentView(): View {
        return contentLayout!!
    }

    /**
     * Вызов аналогичен #expand(), однако отличается тем, что слушатели
     * продолжат работать во время операции.
     */
    private fun expandInternalImpl() {
        bottomSheet?.let(::createBottomSheetBehavior)
            ?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    /**
     * Вызов аналогичен #collapse(), однако отличается тем, что слушатели
     * продолжат работать во время операции.
     */
    private fun collapseInternalImpl() {
        bottomSheet?.let(::createBottomSheetBehavior)
            ?.state = BottomSheetBehavior.STATE_HIDDEN
    }
}