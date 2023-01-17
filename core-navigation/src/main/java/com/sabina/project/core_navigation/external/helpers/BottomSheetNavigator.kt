package com.sabina.project.core_navigation.external.helpers

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.FragmentNavigator
import java.util.ArrayDeque

/**
 * Код скопирован из оригинального класса FragmentNavigator библиотеки Jetpack Navigation
 */
@Navigator.Name("bottomsheet")
class BottomSheetNavigator(
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val containerId: Int
) : Navigator<FragmentNavigator.Destination>() {

    private val backStack = ArrayDeque<Int>()

    override fun createDestination(): FragmentNavigator.Destination {
        return FragmentNavigator.Destination(this)
    }

    override fun navigate(
        destination: FragmentNavigator.Destination,
        args: Bundle?,
        navOptions: NavOptions?,
        navigatorExtras: Extras?
    ): NavDestination? {
        if (fragmentManager.isStateSaved) {
            Log.i(TAG, "Ignoring navigate() call: FragmentManager has already saved its state")
            return null
        }
        var className = destination.className
        if (className[0] == '.') {
            className = context.packageName + className
        }

        val visibleFragment = fragmentManager.fragments.last()
        if (visibleFragment is BottomSheetFragment) {
            fragmentManager.beginTransaction()
                .hide(visibleFragment)
                .commit()
        }

        val fragment = fragmentManager.fragmentFactory.instantiate(context.classLoader, className)
        fragment.arguments = args

        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(containerId, fragment)
        fragmentTransaction.setPrimaryNavigationFragment(fragment)
        val destId = destination.id
        val initialNavigation = backStack.isEmpty()

        // TODO Build first class singleTop behavior for fragments
        val isSingleTopReplacement = (navOptions != null && !initialNavigation &&
            navOptions.shouldLaunchSingleTop() && backStack.peekLast() == destId)
        val isAdded = when {
            initialNavigation -> true
            isSingleTopReplacement -> {
                // Single Top means we only want one instance on the back stack
                if (backStack.size > 1) {
                    // If the Fragment to be replaced is on the FragmentManager's
                    // back stack, a simple replace() isn't enough so we
                    // remove it from the back stack and put our replacement
                    // on the back stack in its place
                    fragmentManager.popBackStack(
                        generateBackStackName(backStack.size, backStack.peekLast()!!),
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    val backStackName = generateBackStackName(backStack.size, destId)
                    fragmentTransaction.addToBackStack(backStackName)
                }
                false
            }
            else -> {
                val backStackName = generateBackStackName(backStack.size + 1, destId)
                fragmentTransaction.addToBackStack(backStackName)
                true
            }
        }
        if (navigatorExtras is FragmentNavigator.Extras) {
            for ((key, value) in navigatorExtras.sharedElements.entries) {
                fragmentTransaction.addSharedElement(key, value)
            }
        }
        fragmentTransaction.setReorderingAllowed(true)
        fragmentTransaction.commit()
        // The commit succeeded, update our view of the world
        return if (isAdded) {
            backStack.add(destId)
            destination
        } else {
            null
        }
    }

    override fun popBackStack(): Boolean {
        if (backStack.isEmpty()) {
            return false
        }
        if (fragmentManager.isStateSaved) {
            Log.i(TAG, "Ignoring popBackStack() call: FragmentManager has already saved its state")
            return false
        }
        val fragments = fragmentManager.fragments
        val topFragment = fragments[fragments.lastIndex]
        val underTopFragment = fragments[fragments.lastIndex - 1]
        fragmentManager.beginTransaction().apply {
            if (topFragment is BottomSheetFragment) {
                remove(topFragment)
            }
            if (underTopFragment is BottomSheetFragment) {
                show(underTopFragment)
                // expand previous fragment because it's probably hidden
                // and it's onViewCreated won't be invoked twice
                underTopFragment.expand()
            }
            commit()
        }
        backStack.removeLast()
        return true
    }

    override fun onSaveState(): Bundle {
        return Bundle().apply {
            val backStackIds = IntArray(backStack.size)
            var index = 0
            for (id in backStack) {
                backStackIds[index++] = id
            }
            putIntArray(KEY_BACK_STACK_IDS, backStackIds)
        }
    }

    override fun onRestoreState(savedState: Bundle) {
        super.onRestoreState(savedState)
        val backStackIds = savedState.getIntArray(KEY_BACK_STACK_IDS)
        if (backStackIds != null) {
            backStack.clear()
            for (destId in backStackIds) {
                backStack.add(destId)
            }
        }
    }

    private fun generateBackStackName(backStackIndex: Int, destId: Int): String {
        return "$backStackIndex-$destId"
    }

    companion object {
        private const val TAG = "BottomSheetNavigator"
        private const val KEY_BACK_STACK_IDS = "androidx-nav-fragment:navigator:backStackIds"
    }
}