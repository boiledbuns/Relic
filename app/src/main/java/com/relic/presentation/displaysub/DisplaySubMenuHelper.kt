package com.relic.presentation.displaysub

import com.relic.R
import com.relic.data.PostRepository
import com.relic.data.SortScope
import com.relic.data.SortType

object DisplaySubMenuHelper {
    const val sortMenuId = R.id.display_sub_sort
    val sortMethodSubMenuIdsWithScope = intArrayOf(R.id.post_sort_hot, R.id.post_sort_rising, R.id.post_sort_top)

    const val userSortMenuId = R.id.display_user_sort
    val sortMethodUserMenuIdsWithScope = intArrayOf(R.id.user_sort_hot, R.id.user_sort_top)

    // region menu item id helpers

    fun convertMenuItemToSortType(optionId : Int) : SortType {
        return when(optionId) {
            R.id.post_sort_best -> SortType.BEST
            R.id.post_sort_hot, R.id.user_sort_hot -> SortType.HOT
            R.id.post_sort_new, R.id.user_sort_new -> SortType.NEW
            R.id.post_sort_rising -> SortType.RISING
            R.id.post_sort_top, R.id.user_sort_top -> SortType.TOP
            R.id.post_sort_controversial, R.id.user_sort_controversial -> SortType.CONTROVERSIAL
            else -> SortType.DEFAULT
        }
    }

    fun convertMenuItemToSortScope(optionId : Int) : SortScope {
        return when(optionId) {
            R.id.order_scope_hour -> SortScope.HOUR
            R.id.order_scope_day -> SortScope.DAY
            R.id.order_scope_week -> SortScope.WEEK
            R.id.order_scope_month -> SortScope.MONTH
            R.id.order_scope_year -> SortScope.YEAR
            R.id.order_scope_all -> SortScope.ALL
            else -> SortScope.NONE
        }
    }

    // end region menu item id helpers

    // region text helpers

    fun convertSortingTypeToText(sortByCode : SortType) : String  {
        return when(sortByCode) {
            SortType.DEFAULT-> "best"
            SortType.HOT -> "hot"
            SortType.NEW -> "new"
            SortType.RISING-> "rising"
            SortType.TOP -> "top"
            SortType.CONTROVERSIAL -> "controversial"
            else -> "default"
        }
    }

    fun convertSortingScopeToText(sortByScope : SortScope) : String? {
        return when(sortByScope) {
            SortScope.HOUR-> "hour"
            SortScope.DAY -> "day"
            SortScope.WEEK -> "week"
            SortScope.MONTH-> "month"
            SortScope.YEAR -> "year"
            SortScope.ALL -> "all"
            else -> null
        }
    }

    // region text helpers

}