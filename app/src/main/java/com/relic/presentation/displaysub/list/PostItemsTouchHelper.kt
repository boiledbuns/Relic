package com.relic.presentation.displaysub.list

import android.graphics.Canvas
import android.os.Handler
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import com.relic.presentation.displaysub.DisplaySubContract
import kotlinx.coroutines.delay

class PostItemsTouchHelper(
    private val delegate : DisplaySubContract.PostAdapterDelegate
) : ItemTouchHelper.Callback() {

    override fun getMovementFlags(p0: RecyclerView, p1: RecyclerView.ViewHolder): Int {
        return ItemTouchHelper.Callback.makeFlag(
            ItemTouchHelper.ACTION_STATE_SWIPE,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        )
    }

    override fun onMove(
        p0: RecyclerView,
        p1: RecyclerView.ViewHolder,
        p2: RecyclerView.ViewHolder
    ): Boolean {
        // return false to indicate that we nothing has been moved
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val postItemVh = viewHolder as PostItemVH

        when (direction) {
            ItemTouchHelper.LEFT -> delegate.voteOnPost(postItemVh.itemFullName,  1)
            ItemTouchHelper.RIGHT -> delegate.voteOnPost(postItemVh.itemFullName,  -1)
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        when (actionState) {
            ItemTouchHelper.ACTION_STATE_SWIPE -> {
                viewHolder.itemView.translationX = dX / 3
            }
            else -> super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }
}