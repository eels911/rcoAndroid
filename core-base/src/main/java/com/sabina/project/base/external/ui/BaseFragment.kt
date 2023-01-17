package com.sabina.project.base.external.ui

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

abstract class BaseFragment(@LayoutRes res: Int) : Fragment(res) {
    override fun onDestroy() {
        super.onDestroy()
        (requireActivity() as? KeyboardManager)?.hideKeyboard()
    }
}