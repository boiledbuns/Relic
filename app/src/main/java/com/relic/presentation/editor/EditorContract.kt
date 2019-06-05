package com.relic.presentation.editor

interface  EditorContract {
    interface EditorDelegate {
        fun submit(text : String)
        fun saveChanges(text : String)
    }

    interface ViewModel {

    }

    data class ReplyParent(
        val title : String? = null,
        val body : String? = null
    )

    enum class ParentType { POST, COMMENT }
}