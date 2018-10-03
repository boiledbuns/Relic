package com.relic.presentation.editor

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import com.relic.data.CommentRepository
import com.relic.data.PostRepository
import com.relic.presentation.editor.EditorContract.*
import javax.inject.Inject

class EditorVM constructor(
        private val postRepo : PostRepository,
        private val commentRepo: CommentRepository
): EditorContract.VM, ViewModel() {

    class Factory @Inject constructor(
            private val postRepo : PostRepository,
            private val commentRepo: CommentRepository
    ) {
        fun create () : EditorVM {
            return EditorVM(postRepo, commentRepo)
        }
    }

    private val _parentModel = MediatorLiveData<ReplyParent>()
    val parentModel : LiveData<ReplyParent> = _parentModel

    override fun init(
            subName: String,
            fullName: String,
            parentType: Int) {

        if (parentType == EditorContract.VM.POST_PARENT) {
            // retrieve the post from the post repo
            _parentModel.addSource(postRepo.getPost(fullName)) {
                Log.d("editorvm", "fullname = " + fullName + ": " + it?.selftext)
                _parentModel.postValue(ReplyParent(it?.title, it?.htmlSelfText))
            }
        }
        else if (parentType == EditorContract.VM.COMMENT_PARENT) {
            // TODO add method for retrieving comment model
            //commentRepo.get
        }

    }





}