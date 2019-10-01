package com.relic.presentation.editor

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

interface  EditorContract {
    interface Editor {
        fun submit()
        fun saveChanges()
        fun onBodyChanged(newBody : String)
    }

    interface ReplyEditor : Editor

    interface NewPostEditor : Editor {
        fun onTitleChanged(newTitle : String)
    }

    data class ReplyParent(
        val title : String? = null,
        val body : String? = null
    )

    sealed class EditorOption : Parcelable{
        @Parcelize
        data class NewPost (val subreddit : String) : EditorOption()

        @Parcelize
        data class CommentReply(val parent : String, val parentIsPost : Boolean = false) : EditorOption()
    }
}