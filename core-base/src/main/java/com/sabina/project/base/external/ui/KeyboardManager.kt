package com.sabina.project.base.external.ui

import android.widget.EditText

interface KeyboardManager {
	fun hideKeyboard()
	fun showKeyboard(editText: EditText)
}