package com.relic.presentation.displaysub

import com.relic.R
import com.relic.data.PostRepository

object DisplaySubMenuHelper {
    val sortMenuId = R.id.display_sub_sort
    val sortMethodSubMenuIdsWithScope = listOf(R.id.post_sort_hot, R.id.post_sort_rising, R.id.post_sort_top)

    fun convertMenuItemToSortType(optionId : Int) : Int {
        return when(optionId) {
            R.id.post_sort_best -> PostRepository.SORT_BEST
            R.id.post_sort_hot -> PostRepository.SORT_HOT
            R.id.post_sort_new -> PostRepository.SORT_NEW
            R.id.post_sort_rising -> PostRepository.SORT_RISING
            R.id.post_sort_top -> PostRepository.SORT_TOP
            R.id.post_sort_controversial -> PostRepository.SORT_CONTROVERSIAL
            else -> PostRepository.SORT_DEFAULT
        }
    }

    fun convertMenuItemToSortScope(optionId : Int) : Int{
        return when(optionId) {
            R.id.order_scope_hour -> PostRepository.SCOPE_HOUR
            R.id.order_scope_day -> PostRepository.SCOPE_DAY
            R.id.order_scope_week -> PostRepository.SCOPE_WEEK
            R.id.order_scope_month -> PostRepository.SCOPE_MONTH
            R.id.order_scope_year -> PostRepository.SCOPE_YEAR
            R.id.order_scope_all -> PostRepository.SCOPE_ALL
            else -> PostRepository.SCOPE_NONE
        }
    }
}