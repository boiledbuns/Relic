package com.relic.presentation.displaysub.list

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.os.Handler
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import com.relic.R
import com.relic.presentation.displaysub.DisplaySubContract
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

class PostItemsTouchHelper(
    private val delegate : DisplaySubContract.PostAdapterDelegate,
    private val context : Context
) : ItemTouchHelper.Callback() {

    // initialize values needed for swipe so we don't have to retrieve them each time
    val swipeRightColor  = context.resources.getColor(R.color.upvote)
    val swipeLeftColor  = context.resources.getColor(R.color.downvote)
    val marginTop = context.resources.getDimension(R.dimen.padding_xs).roundToInt()
    val upvoteIcon = context.getDrawable(R.drawable.ic_upvote)
    val downvoteIcon = context.getDrawable(R.drawable.ic_downvote)

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
            // might need to go through adapter to indicate change
            ItemTouchHelper.LEFT -> {
                delegate.voteOnPost(postItemVh.itemFullName,  -1)
                postItemVh.postItemView.setVote(-1)
            }
            ItemTouchHelper.RIGHT -> {
                delegate.voteOnPost(postItemVh.itemFullName,  1)
                postItemVh.postItemView.setVote(1)
            }
        }
    }

    override fun onChildDraw(
        canvas: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        when (actionState) {
            ItemTouchHelper.ACTION_STATE_SWIPE -> {
                viewHolder.itemView.translationX = dX.div(1.5).toFloat()

                canvas.apply {
                    // clip the canvas to the height of the item in the list
                    clipRect(
                        viewHolder.itemView.left.toFloat(),
                        viewHolder.itemView.top.toFloat() + marginTop,
                        viewHolder.itemView.right.toFloat(),
                        viewHolder.itemView.bottom.toFloat()
                    )

                    val vhHeight = viewHolder.itemView.bottom- viewHolder.itemView.top

                    if (dX > 0) {
                        drawColor(swipeRightColor)
                        // left, top, right, bottom
                        upvoteIcon.bounds = Rect(
                            marginTop,
                            viewHolder.itemView.top + marginTop,
                            marginTop + vhHeight/2,
                            viewHolder.itemView.top + marginTop + vhHeight/2
                        )
                        upvoteIcon.draw(canvas)
                    }
                    else if (dX < 0) {
                        drawColor(swipeLeftColor)
                        // left, top, right, bottom
                        downvoteIcon.bounds = Rect(
                            marginTop,
                            viewHolder.itemView.top + marginTop,
                            marginTop + vhHeight/2,
                            viewHolder.itemView.top + marginTop + vhHeight/2
                        )
                        downvoteIcon.draw(canvas)
                    }

                }
            }
            else -> super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }
}