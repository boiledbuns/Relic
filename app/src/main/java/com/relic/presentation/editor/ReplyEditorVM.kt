package com.relic.presentation.editor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.relic.data.CommentRepository
import com.relic.data.PostRepository
import com.relic.presentation.base.RelicViewModel
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class ReplyEditorVM(
    private val postRepo : PostRepository,
    private val commentRepo : CommentRepository,
    private val parent : String,
    private val parentIsPost : Boolean
) : RelicViewModel() {

    class Factory @Inject constructor(
        private val postRepo : PostRepository,
        private val commentRepo: CommentRepository
    ) {
        fun create (parent: String, parentIsPost : Boolean) : ReplyEditorVM {
            return ReplyEditorVM(postRepo, commentRepo, parent, parentIsPost)
        }
    }

    private val _replyParentLiveData = MediatorLiveData<EditorContract.ReplyParent>()
    val replyParentLiveData : LiveData<EditorContract.ReplyParent> = _replyParentLiveData


    override fun handleException(context: CoroutineContext, e: Throwable) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}