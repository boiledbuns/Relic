package com.relic.data

/**
 * helper class for deserializing post repo types like sort type and scope from string
 * necessary for instances where we can't parcelize them (ie. worker params)
 */
object PostRepoHelper {
    fun toSortType(value : String?) : SortType {
        return when (value) {
            "BEST" -> SortType.BEST
            "CONTROVERSIAL" -> SortType.CONTROVERSIAL
            "HOT" -> SortType.HOT
            "NEW" -> SortType.NEW
            "RISING" -> SortType.RISING
            "TOP "-> SortType.TOP
            else -> SortType.DEFAULT
        }
    }

    fun toSortScope(value : String?) : SortScope {
        return when (value) {
            "NONE" -> SortScope.HOUR
            "DAY" -> SortScope.DAY
            "WEEK" -> SortScope.WEEK
            "MONTH" -> SortScope.MONTH
            "YEAR" -> SortScope.YEAR
            "ALL" -> SortScope.ALL
            else -> SortScope.NONE
        }
    }
}