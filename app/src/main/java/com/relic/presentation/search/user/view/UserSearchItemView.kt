package com.relic.presentation.search.user.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.relic.R
import com.relic.domain.models.UserModel
import kotlinx.android.synthetic.main.user_search_item.view.*

class UserSearchItemView @JvmOverloads constructor(
        context: Context,
        attrs : AttributeSet? = null,
        defStyleAttr : Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {
    init {
        LayoutInflater.from(context).inflate(R.layout.user_search_item, this, true)
    }

    fun bind(user : UserModel) {
        username.text = user.fullName
    }
}