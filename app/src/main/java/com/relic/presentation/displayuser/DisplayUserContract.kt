package com.relic.presentation.displayuser

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

interface DisplayUserContract {
    interface View {}
    interface ViewModel {}
}

sealed class UserTab(
    val tabName : String
) : Parcelable {

    @Parcelize object Submitted: UserTab("Submitted")
    @Parcelize object Comments: UserTab("Comments")
    @Parcelize object Saved: UserTab("Saved")
    @Parcelize object Upvoted: UserTab("Upvoted")
    @Parcelize object Downvoted: UserTab("Downvoted")
    @Parcelize object Gilded: UserTab("Gilded")
    @Parcelize object Hidden: UserTab("Hidden")
}