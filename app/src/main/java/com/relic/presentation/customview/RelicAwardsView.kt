package com.relic.presentation.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.setPadding
import com.relic.R
import com.relic.domain.models.Award
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.awards_view.view.*
import kotlinx.android.synthetic.main.post_item_content.view.*

class RelicAwardsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {
    private val iconSize = 72
    private val iconLayoutParams = LinearLayout.LayoutParams(iconSize,iconSize)
    init {
        LayoutInflater.from(context).inflate(R.layout.awards_view, this, true)
    }

    fun setAwards(awards: List<Award>?) {
        root.removeAllViews()
        if (!awards.isNullOrEmpty()) {
            root.visibility = View.VISIBLE
            awards.forEach { award ->
                val awardImageView = ImageView(context)
                Picasso.get().load(award.icon_url).fit().into(awardImageView)
                awardImageView.tooltipText = award.name
                awardImageView.layoutParams = iconLayoutParams
                awardImageView.setPadding(10)
                root.addView(awardImageView)

                if (award.count > 1) {
                    val countTextView = TextView(context)
                    countTextView.text = "x${award.count}"
                    countTextView.setPadding(0, 10, 16, 0)
                    root.addView(countTextView)
                }
            }
        } else {
            root.visibility = View.GONE
        }
    }
}