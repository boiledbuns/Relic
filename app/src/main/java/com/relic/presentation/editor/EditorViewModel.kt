package com.relic.presentation.editor

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import com.relic.data.CommentRepository
import com.relic.data.PostRepository
import com.relic.presentation.editor.EditorContract.*
import javax.inject.Inject

class EditorViewModel constructor(
    private val postRepo : PostRepository,
    private val commentRepo: CommentRepository,
    private val subName: String,
    private val fullName: String,
    private val parentType: ParentType
): EditorContract.ViewModel, ViewModel() {

    class Factory @Inject constructor(
            private val postRepo : PostRepository,
            private val commentRepo: CommentRepository
    ) {
        fun create (subName: String, fullName: String, parentType: ParentType) : EditorViewModel {
            return EditorViewModel(postRepo, commentRepo, subName, fullName, parentType)
        }
    }

    private val _replyParentLiveData = MediatorLiveData<ReplyParent>()
    val replyParentLiveData : LiveData<ReplyParent> = _replyParentLiveData

    private var currentBody = ""

    init {
        when (parentType) {
            ParentType.POST -> {
                // retrieve the post from the post repo
                _replyParentLiveData.addSource(postRepo.getPost(fullName)) {
                    Log.d("editorvm", "fullname = " + fullName + ": " + it?.selftext)
                    _replyParentLiveData.postValue(ReplyParent(it?.title, it?.selftext))
                }
            }
            ParentType.COMMENT -> {
                // TODO add method for retrieving comment model
                //commentRepo.get
            }

        }
    }

    fun onTextChanged(newText : String) {
        currentBody = newText
    }

    fun submitText() {

    }
}