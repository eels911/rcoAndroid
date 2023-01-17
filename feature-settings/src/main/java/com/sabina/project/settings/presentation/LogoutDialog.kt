package com.sabina.project.settings.presentation

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.sabina.project.base.external.extensions.html
import com.sabina.project.core_navigation.external.Screen
import com.sabina.project.core_navigation.external.helpers.navHostFragmentManager
import com.sabina.project.settings.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class LogoutDialog : DialogFragment() {

    companion object {
        const val TAG = "LogoutDialog"
        private const val RESULT_KEY = "RESULT_KEY"

        fun getResult(bundle: Bundle): Result = Result.enumValueOf(bundle.getString(RESULT_KEY)!!)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
            .setTitle(getString(R.string.logout_dialog_title))
            .setMessage(getString(R.string.logout_dialog_subtitle).html())
            .setNegativeButton(R.string.cancel) { _, _ -> onClose(Result.NEGATIVE) }
            .setPositiveButton(R.string.confirm) { _, _ -> onClose(Result.POSITIVE) }
            .show()
    }

    private fun onClose(result: Result) {
        val bundle = bundleOf(RESULT_KEY to result.name)
        navHostFragmentManager().setFragmentResult(TAG, bundle)
    }

    enum class Result {
        NEGATIVE,
        POSITIVE;

        companion object {
            fun enumValueOf(name: String): Result {
                return values().first { it.name == name }
            }
        }
    }

    object NavScreen : Screen(
        route = "rco-android-app://com.sabina.project/root_settings/logout_dialog",
        requestKey = TAG
    )
}