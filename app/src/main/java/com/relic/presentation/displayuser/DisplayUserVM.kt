package com.relic.presentation.displayuser

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.relic.data.CommentRepository
import com.relic.data.ListingRepository
import com.relic.data.PostRepository
import com.relic.data.models.PostModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class DisplayUserVM(
    private val postRepo: PostRepository,
    private val commentRepo: CommentRepository,
    private val listingRepo: ListingRepository,
    private val username : String
) : ViewModel() {

    class Factory @Inject constructor(
        private val postRepo: PostRepository,
        private val commentRepo: CommentRepository,
        private val listingRepo: ListingRepository
    ) {
        fun create(username : String) : DisplayUserVM {
            return DisplayUserVM(postRepo, commentRepo, listingRepo, username)
        }
    }

    private val _submissionLiveData  = MutableLiveData<PostModel>()
    val submissionLiveData : LiveData<PostModel> = _submissionLiveData

    init {
        GlobalScope.launch {
            postRepo.retrieveUserSubmissions(username)
        }
    }

}