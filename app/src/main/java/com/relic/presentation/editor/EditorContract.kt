package com.relic.presentation.editor

interface  EditorContract {

    interface VM {
        companion object {
            const val POST_PARENT = 0
            const val COMMENT_PARENT = 1
        }

        fun init(subName: String, fullName: String, parentType : Int)
    }

    data class ReplyParent(
            val title : String? = null,
            val body : String? = null
    )
}