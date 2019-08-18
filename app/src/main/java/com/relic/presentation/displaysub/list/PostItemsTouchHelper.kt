package com.relic.presentation.displaysub.list

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import com.relic.R
import com.relic.presentation.displaysub.DisplaySubFragment
import timber.log.Timber
import kotlin.math.roundToInt

// TODO add custom lengths for swipe and release actions - currently any length will trigger
class PostItemsTouchHelper(
    private val fragment: DisplaySubFragment,
    private val context : Context
) : ItemTouchHelper.Callback() {

    private val translationScale = 0.5
    private var previousXTranslation = 0F

    // initialize values needed for swipe so we don't have to retrieve them each time
    private val swipeRightColor  = context.resources.getColor(R.color.upvote)
    private val swipeLeftColor  = context.resources.getColor(R.color.downvote)
    private val marginTop = context.resources.getDimension(R.dimen.padding_xs).roundToInt()
    private val upvoteIcon = context.getDrawable(R.drawable.ic_upvote)
    private val downvoteIcon = context.getDrawable(R.drawable.ic_downvote)

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
        // full swipe not actually allowed because I currently don't have
        // any good solutions to return the viewholder to its original position
        // notifyItemChanged works, but it flashes which makes for terrible ux
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return 10F
    }

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        // see onSwiped method
        return Float.MAX_VALUE
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
        Timber.d("touch helper %s", dX.toString())
        when (actionState) {
            ItemTouchHelper.ACTION_STATE_SWIPE -> {
                viewHolder.itemView.translationX = dX.times(translationScale).toFloat()

                canvas.apply {
                    // clip the canvas to the height of the item in the list
                    clipRect(
                        viewHolder.itemView.left.toFloat(),
                        viewHolder.itemView.top.toFloat() + marginTop,
                        viewHolder.itemView.right.toFloat(),
                        viewHolder.itemView.bottom.toFloat()
                    )

                    val vhHeight = viewHolder.itemView.bottom - viewHolder.itemView.top

                    if (dX == 0F) {
                        // from right swipe to release
                        if (previousXTranslation > 0F) {
                            Timber.d("touch helper right %s", dX.toString())
                            fragment.handleVHSwipeAction(viewHolder, ItemTouchHelper.RIGHT)
                        } else if (previousXTranslation < 0F){
                            Timber.d("touch helper left %s", dX.toString())
                            fragment.handleVHSwipeAction(viewHolder, ItemTouchHelper.LEFT)
                        }
                    }
                    else if (dX > 0) {
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
                        downvoteIcon.bounds = Rect(
                            marginTop,
                            viewHolder.itemView.top + marginTop,
                            marginTop + vhHeight/2,
                            viewHolder.itemView.top + marginTop + vhHeight/2
                        )
                        downvoteIcon.draw(canvas)
                    }

                }

                previousXTranslation = dX
            }
            else -> super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }
}