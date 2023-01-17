package com.sabina.project.project_manager.presentation.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.sabina.project.base.external.extensions.html
import com.sabina.project.core_navigation.external.helpers.navHostFragmentManager
import com.sabina.project.project_manager.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class RationalPermissionDialog : DialogFragment() {

    private val args: RationalPermissionDialogArgs by navArgs()

    companion object {
        const val TAG = "RationalPermissionDialog"
        private const val RESULT_KEY = "RESULT_KEY"

        fun getResult(bundle: Bundle): Result = Result.enumValueOf(bundle.getString(RESULT_KEY)!!)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
            .setTitle(args.title)
            .setMessage(getString(R.string.rational_permission_request_dialog_subtitle).html())
            .setNegativeButton(R.string.cancel) { _, _ -> onClose(Result.NEGATIVE) }
            .setPositiveButton(R.string.settings) { _, _ -> onClose(Result.POSITIVE) }
            .show()
    }

    private fun onClose(result: Result) {
        val bundle = bundleOf(RESULT_KEY to result.name)
        navHostFragmentManager().setFragmentResult(TAG, bundle)
        findNavController().popBackStack()
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
}