package com.relic.presentation.base

/**
 * use for custom views in viewholders to tell viewholder to
 * notify adapter that an item update has occured
 */
interface ItemNotifier {
    fun notifyItem()
}