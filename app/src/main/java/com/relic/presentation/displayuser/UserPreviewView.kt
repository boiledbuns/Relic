package com.relic.presentation.displayuser

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.relic.R
import com.relic.data.models.UserModel
import com.relic.presentation.helper.DateHelper
import kotlinx.android.synthetic.main.user_preview_view.view.*
import java.text.SimpleDateFormat
import java.util.*

class UserPreviewView @JvmOverloads constructor(
    context : Context,
    attrs: AttributeSet? = null,
    defStyleAttr : Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private val TAG = "USER_PREVIEW_VIEW"

    init {
        LayoutInflater.from(context).inflate(R.layout.user_preview_view, this, true)
    }

    fun setUser (userModel : UserModel) {
        userPreviewLinkKarma.text = userModel.linkKarma.toString()
        userPreviewCommentKarma.text = userModel.commentKarma.toString()
        userPreviewTotalKarma.text = (userModel.linkKarma + userModel.commentKarma).toString()

        userPreviewCreated.text = getUserCreatedString(userModel.created.toDouble().toLong())
    }

    private fun getUserCreatedString(created : Long) : String {
        // initialize the date formatter and date for "now"
        val formatter = SimpleDateFormat("MMM dd',' YYYY", Locale.CANADA)
        val createdDate = Date(created * 1000)

        val userAge = DateHelper.getDateDifferenceString(createdDate, Date())
        val userCreationDate = formatter.format(createdDate)

        return resources.getString(R.string.account_age, userAge, userCreationDate)
    }
}