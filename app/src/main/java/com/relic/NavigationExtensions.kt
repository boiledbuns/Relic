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

package com.relic

import android.content.Intent
import android.util.SparseArray
import android.view.MenuItem
import androidx.core.util.forEach
import androidx.core.util.set
import androidx.core.view.forEachIndexed
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

// mapping between items and tag of fragments currently being managed
val itemIdToTagMap = SparseArray<String>()

// Result. Mutable live data with the selected controlled
private val selectedNavController = MutableLiveData<NavController>()

/**
 * Manages the various graphs needed for a [BottomNavigationView].
 *
 * This sample is a workaround until the Navigation Component supports multiple back stacks.
 */
fun BottomNavigationView.initializeNavHostFragments(
    fragmentManager: FragmentManager,
    containerId: Int,
    initialItemId: Int?,
    menuItemToDestinationMap: Map<Int, Int>,
    onItemReselected: (menuItem: MenuItem) -> Unit
): LiveData<NavController> {
    // set initial item id if it's specified (ie. not restored)
    initialItemId?.let { selectedItemId = it }
    val shouldRestore = initialItemId == null

    if (!shouldRestore) {
        // First create a NavHostFragment for each NavGraph ID
        this.menu.forEachIndexed { index, item ->
            val fragmentTag = getFragmentTag(index)
            val itemId = item.itemId

            // Find or create the Navigation host fragment
            obtainNavHostFragment(fragmentManager, fragmentTag, itemId, containerId).let { navHostFragment ->
                // replace the placeholder fragment with the actual fragment associated with the menu item
                // and remove it from the back stack
                menuItemToDestinationMap[itemId]?.let { destinationId ->
                    navHostFragment.findNavController().apply {
                        popBackStack()
                        navigate(destinationId)
                    }
                }
                // Save to the map
                itemIdToTagMap[itemId] = fragmentTag

                // Attach or detach nav host fragment depending on whether it's the selected item.
                if (selectedItemId == itemId) {
                    attachNavHostFragment(fragmentManager, navHostFragment, true)
                } else {
                    detachNavHostFragment(fragmentManager, navHostFragment)
                }
            }
        }
    }

    // Update livedata with the selected graph
    val selectedTag = itemIdToTagMap[selectedItemId]
    selectedNavController.value = obtainNavHostFragment(fragmentManager, selectedTag, selectedItemId, containerId).navController

    // Now connect selecting an item with swapping Fragments
    var selectedItemTag = itemIdToTagMap[selectedItemId]
    val firstFragmentTag = itemIdToTagMap[selectedItemId]
    var isOnFirstFragment = selectedItemTag == firstFragmentTag

    // When a navigation item is selected
    setOnNavigationItemSelectedListener { item ->
        // Don't do anything if the state is state has already been saved.
        if (fragmentManager.isStateSaved) {
            false
        } else {
            val originalSelectedItemTag = itemIdToTagMap[selectedItemId]
            val newlySelectedItemTag = itemIdToTagMap[item.itemId]
            if (originalSelectedItemTag != newlySelectedItemTag) {
                // Pop everything including the first fragment
                fragmentManager.popBackStack(firstFragmentTag, FragmentManager.POP_BACK_STACK_INCLUSIVE)

                val selectedFragment = fragmentManager.findFragmentByTag(newlySelectedItemTag) as NavHostFragment

                // Commit a transaction that cleans the back stack and adds the fragment associated
                // with the current nav item to it
                fragmentManager.beginTransaction()
                    .attach(selectedFragment)
                    .setPrimaryNavigationFragment(selectedFragment)
                    .apply {
                        // Detach all other Fragments
                        itemIdToTagMap.forEach { _, fragmentTag ->
                            if (fragmentTag != newlySelectedItemTag) {
                                detach(fragmentManager.findFragmentByTag(fragmentTag)!!)
                            }
                        }
                    }
                    .setReorderingAllowed(true)
                    .commit()
                selectedItemTag = newlySelectedItemTag
                selectedNavController.value = selectedFragment.navController
                true
            } else {
                false
            }
        }
    }

    // Optional: on item reselected, pop back stack to the destination of the graph
    setOnNavigationItemReselectedListener(onItemReselected)

    // Handle deep link
//    setupDeepLinks(navGraphIds, fragmentManager, containerId, intent)

    return selectedNavController
}

fun BottomNavigationView.resetBottomNavigation() {
    setOnNavigationItemSelectedListener(null)
    setOnNavigationItemReselectedListener(null)
    itemIdToTagMap.clear()
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
