package com.relic.presentation.displayuser

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.relic.R
import com.relic.domain.models.UserModel
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

        userPreviewCreated.text = getUserCreatedString(userModel.created!!)
    }

    private fun getUserCreatedString(createdDate : Date) : String {
        // initialize the date formatter and date for "now"
        val formatter = SimpleDateFormat("MMM dd',' YYYY", Locale.CANADA)

        val userAge = DateHelper.getDateDifferenceString(createdDate, Date())
        val userCreationDate = formatter.format(createdDate)

        return resources.getString(R.string.account_age, userAge, userCreationDate)
    }
}