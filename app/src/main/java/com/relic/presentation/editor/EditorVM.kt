package com.relic.presentation.editor

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import com.relic.data.CommentRepository
import com.relic.data.PostRepository
import javax.inject.Inject

class EditorVM @Inject constructor(
        private val postRepo : PostRepository,
        private val commentRepo: CommentRepository
): EditorContract.VM, ViewModel() {

    private var isInitialized : Boolean = false
    private val parentModelText = MediatorLiveData<String>()

    override fun isInitialized(): Boolean = isInitialized

    override fun init(
            subName: String,
            fullName: String,
            parentType: Int) {

        if (parentType == EditorContract.VM.POST_PARENT) {
            // retrieve the post from the post repo
            parentModelText.addSource(postRepo.getPost(fullName)) {
                Log.d("editorvm", "fullname = " + fullName + ": " + it?.selftext)
                parentModelText.value = it?.htmlSelfText
            }
        }
        else if (parentType == EditorContract.VM.COMMENT_PARENT) {
            // TODO add method for retrieving comment model
            //commentRepo.get
        }

        isInitialized = true
    }

    override fun getParentText(): LiveData<String> {
        return parentModelText
    }




}