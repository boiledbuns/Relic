package com.relic.presentation.displaysub.list

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.relic.R
import timber.log.Timber
import kotlin.math.roundToInt

// TODO add custom lengths for swipe and release actions - currently any length will trigger
class PostItemsTouchHelper(
    private val context: Context,
    private val swipeCallback: (vh: RecyclerView.ViewHolder, direction: Int) -> Unit
) : ItemTouchHelper.Callback() {

    private val translationScale = 0.5
    private var previousXTranslation = 0F

    // initialize values needed for swipe so we don't have to retrieve them each time
    private val swipeRightColor = context.resources.getColor(R.color.upvote)
    private val swipeLeftColor = context.resources.getColor(R.color.downvote)
    private val marginTop = context.resources.getDimension(R.dimen.padding_xs).roundToInt()
    private val iconColor = context.resources.getColor(R.color.white)
    private val upvoteIcon = context.getDrawable(R.drawable.ic_upvote)!!
    private val downvoteIcon = context.getDrawable(R.drawable.ic_downvote)!!

    override fun getMovementFlags(p0: RecyclerView, p1: RecyclerView.ViewHolder): Int {
        return makeFlag(
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
                    val icWidth = vhHeight / 2
                    val icHeight = icWidth

                    if (dX == 0F) {
                        // from right swipe to release
                        if (previousXTranslation > 0F) {
                            Timber.d("touch helper right %s", dX.toString())
                            swipeCallback.invoke(viewHolder, ItemTouchHelper.RIGHT)
                        } else if (previousXTranslation < 0F) {
                            Timber.d("touch helper left %s", dX.toString())
                            swipeCallback.invoke(viewHolder, ItemTouchHelper.LEFT)
                        }
                    } else if (dX > 0) {
                        drawColor(swipeRightColor)
                        // left, top, right, bottom
                        upvoteIcon.apply {
                            bounds = Rect(
                                marginTop,
                                viewHolder.itemView.top + vhHeight / 2 - icHeight / 2,
                                marginTop + icWidth,
                                viewHolder.itemView.bottom - vhHeight / 2 + icHeight / 2
                            )
                            setTint(iconColor)
                            draw(canvas)
                        }
                    } else if (dX < 0) {
                        drawColor(swipeLeftColor)
                        downvoteIcon.apply {
                            bounds = Rect(
                                viewHolder.itemView.right - icWidth - marginTop,
                                viewHolder.itemView.top + vhHeight / 2 - icHeight / 2,
                                viewHolder.itemView.right - marginTop,
                                viewHolder.itemView.bottom - vhHeight / 2 + icHeight / 2
                            )
                            setTint(iconColor)
                            draw(canvas)
                        }
                    }

                }

                previousXTranslation = dX
            }
            else -> super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }
}