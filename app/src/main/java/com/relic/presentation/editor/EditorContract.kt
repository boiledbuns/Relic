package com.relic.presentation.editor

import android.arch.lifecycle.LiveData
import com.relic.data.CommentRepository
import com.relic.data.PostRepository

interface  EditorContract {
    interface VM {
        companion object {
            const val POST_PARENT = 0
            const val COMMENT_PARENT = 1
        }

        fun init(subName: String, fullName: String, parentType : Int)

        fun isInitialized() : Boolean

        fun getParentText() : LiveData<String>
    }

}