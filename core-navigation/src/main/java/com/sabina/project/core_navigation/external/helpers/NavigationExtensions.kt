package com.sabina.project.core_navigation.external.helpers

import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.sabina.project.core_navigation.R
import com.sabina.project.core_navigation.external.Screen

fun AppCompatActivity.obtainNavController(containerId: Int): NavController {
	return obtainInternalNavController(
		context = this,
		fragmentManager = supportFragmentManager,
		containerId = containerId
	)
}

fun NavController.navigateViaScreenRoute(
    screen: Screen,
    extras: Navigator.Extras? = null,
) {
    val options = NavOptions.Builder()
        .setEnterAnim(R.anim.slide_in_right)
        .setExitAnim(R.anim.slide_out_left)
        .setPopEnterAnim(R.anim.slide_in_left)
        .setPopExitAnim(R.anim.slide_out_right)
        .build()

    navigate(Uri.parse(screen.route), options, extras)
}

private fun obtainInternalNavController(
    context: Context,
    fragmentManager: FragmentManager,
    containerId: Int
): NavController {
	// support for <bottomsheet/> destinations
	val bottomSheetNavigator = BottomSheetNavigator(
		context = context,
		fragmentManager = fragmentManager,
		containerId = containerId
	)
	return fragmentManager.findFragmentById(containerId)!!
		.findNavController().apply {
			navigatorProvider.addNavigator(bottomSheetNavigator)
		}
}

fun Fragment.navHostFragmentManager(): FragmentManager {
    if (this is BottomSheetFragment) {
        return parentFragmentManager
    }
    return findNavHostFragment().parentFragmentManager
}

fun Fragment.findNavHostFragment(): NavHostFragment {
    if (this is BottomSheetFragment) {
        return parentFragment?.childFragmentManager?.fragments.orEmpty()
            .find { it is NavHostFragment } as NavHostFragment
    }
    return (parentFragment as? NavHostFragment) ?: parentFragment!!.findNavHostFragment()
}