package com.relic.presentation.editor

interface  EditorContract {

    interface ViewModel {

    }

    data class ReplyParent(
        val title : String? = null,
        val body : String? = null
    )

    enum class ParentType { POST, COMMENT }
}