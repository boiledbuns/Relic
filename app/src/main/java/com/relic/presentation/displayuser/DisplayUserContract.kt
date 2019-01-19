package com.relic.presentation.displayuser

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

interface DisplayUserContract {
    interface View {}
    interface ViewModel {}
}

sealed class UserTab : Parcelable{
    @Parcelize object Submitted: UserTab()
    @Parcelize object Comments: UserTab()
    @Parcelize object Saved: UserTab()
    @Parcelize object Upvoted: UserTab()
    @Parcelize object Downvoted: UserTab()
    @Parcelize object Gilded: UserTab()
    @Parcelize object Hidden: UserTab()
}