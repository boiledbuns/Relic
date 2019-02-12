package com.relic.presentation.displaysub

import com.relic.R
import com.relic.data.PostRepository

object DisplaySubMenuHelper {
    const val sortMenuId = R.id.display_sub_sort
    val sortMethodSubMenuIdsWithScope = intArrayOf(R.id.post_sort_hot, R.id.post_sort_rising, R.id.post_sort_top)

    const val userSortMenuId = R.id.display_user_sort
    val sortMethodUserMenuIdsWithScope = intArrayOf(R.id.user_sort_hot, R.id.user_sort_rising, R.id.user_sort_top)

    // region menu item id helpers

    fun convertMenuItemToSortType(optionId : Int) : PostRepository.SortType {
        return when(optionId) {
            R.id.post_sort_best -> PostRepository.SortType.BEST
            R.id.post_sort_hot -> PostRepository.SortType.HOT
            R.id.post_sort_new -> PostRepository.SortType.NEW
            R.id.post_sort_rising -> PostRepository.SortType.RISING
            R.id.post_sort_top -> PostRepository.SortType.TOP
            R.id.post_sort_controversial -> PostRepository.SortType.CONTROVERSIAL
            else -> PostRepository.SortType.DEFAULT
        }
    }

    fun convertMenuItemToSortScope(optionId : Int) : PostRepository.SortScope{
        return when(optionId) {
            R.id.order_scope_hour -> PostRepository.SortScope.HOUR
            R.id.order_scope_day -> PostRepository.SortScope.DAY
            R.id.order_scope_week -> PostRepository.SortScope.WEEK
            R.id.order_scope_month -> PostRepository.SortScope.MONTH
            R.id.order_scope_year -> PostRepository.SortScope.YEAR
            R.id.order_scope_all -> PostRepository.SortScope.ALL
            else -> PostRepository.SortScope.NONE
        }
    }

    // end region menu item id helpers

    // region text helpers

    fun convertSortingTypeToText(sortByCode : PostRepository.SortType) : String  {
        return when(sortByCode) {
            PostRepository.SortType.DEFAULT-> "best"
            PostRepository.SortType.HOT -> "hot"
            PostRepository.SortType.NEW -> "new"
            PostRepository.SortType.RISING-> "rising"
            PostRepository.SortType.TOP -> "top"
            PostRepository.SortType.CONTROVERSIAL -> "controversial"
            else -> "default"
        }
    }

    fun convertSortingScopeToText(sortByScope : PostRepository.SortScope) : String? {
        return when(sortByScope) {
            PostRepository.SortScope.HOUR-> "hour"
            PostRepository.SortScope.DAY -> "day"
            PostRepository.SortScope.WEEK -> "week"
            PostRepository.SortScope.MONTH-> "month"
            PostRepository.SortScope.YEAR -> "year"
            PostRepository.SortScope.ALL -> "all"
            else -> null
        }
    }

    // region text helpers

}