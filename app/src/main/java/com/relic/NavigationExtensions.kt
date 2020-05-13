/*
 * Copyright 2019, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package navigation

import android.content.Intent
import android.util.SparseArray
import androidx.core.util.forEach
import androidx.core.util.set
import androidx.core.view.forEachIndexed
import androidx.core.view.get
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.relic.R

/**
 * Manages the various graphs needed for a [BottomNavigationView].
 *
 * This sample is a workaround until the Navigation Component supports multiple back stacks.
 */
fun BottomNavigationView.setupWithNavController(
    fragmentManager: FragmentManager,
    containerId: Int,
    intent: Intent,
    initialPosition: Int,
    showLogin: Boolean
): LiveData<NavController> {
    // get the id of the default item from the menu
    val firstItemId = this.menu[initialPosition].itemId
    selectedItemId = firstItemId

    // Map of tags
    val itemIdToTagMap = SparseArray<String>()
    // Result. Mutable live data with the selected controlled
    val selectedNavController = MutableLiveData<NavController>()

    // First create a NavHostFragment for each NavGraph ID
    this.menu.forEachIndexed { index, item ->
        val fragmentTag = getFragmentTag(index)
        val itemId = item.itemId

        // Find or create the Navigation host fragment
        obtainNavHostFragment(
            fragmentManager,
            fragmentTag,
            itemId,
            containerId
        ).let { navHostFragment ->
            // replace the placeholder fragment with the actual fragment associated with the menu item
            // and remove it from the back stack
            when(item.itemId) {
                R.id.nav_account -> R.id.displayUserFragment
                R.id.nav_subs -> R.id.displaySubsFragment
                R.id.nav_home -> R.id.homeFragment
                R.id.nav_search -> R.id.searchFragment
                R.id.nav_settings -> R.id.settingsFragment
                else -> null
            }?.let { destinationId ->
                navHostFragment.findNavController().apply {
                    popBackStack()
                    navigate(destinationId)
                }
            }
            // Save to the map
            itemIdToTagMap[itemId] = fragmentTag

            // Attach or detach nav host fragment depending on whether it's the selected item.
            if (selectedItemId == itemId) {
                // Update livedata with the selected graph
                selectedNavController.value = navHostFragment.navController
                attachNavHostFragment(fragmentManager, navHostFragment, index == initialPosition)
            } else {
                detachNavHostFragment(fragmentManager, navHostFragment)
            }
        }
    }

    // Now connect selecting an item with swapping Fragments
    var selectedItemTag = itemIdToTagMap[this.selectedItemId]
    val firstFragmentTag = itemIdToTagMap[firstItemId]
    var isOnFirstFragment = selectedItemTag == firstFragmentTag

    // When a navigation item is selected
    setOnNavigationItemSelectedListener { item ->
        // Don't do anything if the state is state has already been saved.
        if (fragmentManager.isStateSaved) {
            false
        } else {
            val newlySelectedItemTag = itemIdToTagMap[item.itemId]
            if (selectedItemTag != newlySelectedItemTag) {
                // Pop everything above the first fragment (the "fixed start destination")
                fragmentManager.popBackStack(firstFragmentTag,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE)

                // temp fix, will cleanup later
                val selectedFragment = if (showLogin && item.itemId == R.id.nav_account) {
                    fragmentManager.findFragmentByTag(getFragmentTag(0))
                        as NavHostFragment
                } else {
                    fragmentManager.findFragmentByTag(newlySelectedItemTag)
                        as NavHostFragment
                }

                // Exclude the first fragment tag because it's always in the back stack.
                if (firstFragmentTag != newlySelectedItemTag) {
                    // Commit a transaction that cleans the back stack and adds the first fragment
                    // to it, creating the fixed started destination.
                    fragmentManager.beginTransaction()
                        .attach(selectedFragment)
                        .setPrimaryNavigationFragment(selectedFragment)
                        .apply {
                            // Detach all other Fragments
                            itemIdToTagMap.forEach { _, fragmentTagIter ->
                                if (fragmentTagIter != newlySelectedItemTag) {
                                    detach(fragmentManager.findFragmentByTag(firstFragmentTag)!!)
                                }
                            }
                        }
                        .addToBackStack(firstFragmentTag)
                        .setReorderingAllowed(true)
                        .commit()
                }
                selectedItemTag = newlySelectedItemTag
                isOnFirstFragment = selectedItemTag == firstFragmentTag
                selectedNavController.value = selectedFragment.navController
                true
            } else {
                false
            }
        }
    }

    // Optional: on item reselected, pop back stack to the destination of the graph
    setupItemReselected(itemIdToTagMap, fragmentManager)

    // Handle deep link
//    setupDeepLinks(navGraphIds, fragmentManager, containerId, intent)

    // Finally, ensure that we update our BottomNavigationView when the back stack changes
    fragmentManager.addOnBackStackChangedListener {
        if (!isOnFirstFragment && !fragmentManager.isOnBackStack(firstFragmentTag)) {
            this.selectedItemId = firstItemId
        }

        // Reset the graph if the currentDestination is not valid (happens when the back
        // stack is popped after using the back button).
        selectedNavController.value?.let { controller ->
            if (controller.currentDestination == null) {
                controller.navigate(controller.graph.id)
            }
        }
    }
    return selectedNavController
}

private fun BottomNavigationView.setupDeepLinks(
    navGraphIds: List<Int>,
    fragmentManager: FragmentManager,
    containerId: Int,
    intent: Intent
) {
    navGraphIds.forEachIndexed { index, navGraphId ->
        val fragmentTag = getFragmentTag(index)

        // Find or create the Navigation host fragment
        val navHostFragment = obtainNavHostFragment(
            fragmentManager,
            fragmentTag,
            navGraphId,
            containerId
        )
        // Handle Intent
        if (navHostFragment.navController.handleDeepLink(intent)
            && selectedItemId != navHostFragment.navController.graph.id) {
            this.selectedItemId = navHostFragment.navController.graph.id
        }
    }
}

private fun BottomNavigationView.setupItemReselected(
    graphIdToTagMap: SparseArray<String>,
    fragmentManager: FragmentManager
) {
    setOnNavigationItemReselectedListener { item ->
        val newlySelectedItemTag = graphIdToTagMap[item.itemId]
        val selectedFragment = fragmentManager.findFragmentByTag(newlySelectedItemTag)
            as NavHostFragment
        val navController = selectedFragment.navController
        // Pop the back stack to the start destination of the current navController graph
        navController.popBackStack(
            navController.graph.startDestination, false
        )
    }
}

private fun detachNavHostFragment(
    fragmentManager: FragmentManager,
    navHostFragment: NavHostFragment
) {
    fragmentManager.beginTransaction()
        .detach(navHostFragment)
        .commitNow()
}

private fun attachNavHostFragment(
    fragmentManager: FragmentManager,
    navHostFragment: NavHostFragment,
    isPrimaryNavFragment: Boolean
) {
    fragmentManager.beginTransaction()
        .attach(navHostFragment)
        .apply {
            if (isPrimaryNavFragment) {
                setPrimaryNavigationFragment(navHostFragment)
            }
        }
        .commitNow()

}

private fun obtainNavHostFragment(
    fragmentManager: FragmentManager,
    fragmentTag: String,
    itemId: Int,
    containerId: Int
): NavHostFragment {
    // If the Nav Host fragment exists, return it
    val existingFragment = fragmentManager.findFragmentByTag(fragmentTag) as NavHostFragment?
    existingFragment?.let { return it }

    // Otherwise, create it and return it.
    val navHostFragment = NavHostFragment.create(R.navigation.nav_graph)
    fragmentManager.beginTransaction()
        .add(containerId, navHostFragment, fragmentTag)
        .commitNow()
    return navHostFragment
}

private fun FragmentManager.isOnBackStack(backStackName: String): Boolean {
    val backStackCount = backStackEntryCount
    for (index in 0 until backStackCount) {
        if (getBackStackEntryAt(index).name == backStackName) {
            return true
        }
    }
    return false
}

private fun getFragmentTag(index: Int) = "bottomNavigation#$index"
