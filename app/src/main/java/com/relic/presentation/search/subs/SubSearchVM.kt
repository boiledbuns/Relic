package com.relic.presentation.search.subs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.relic.data.SubRepository
import com.relic.domain.models.PostModel
import com.relic.domain.models.SubredditModel
import com.relic.presentation.base.RelicViewModel
import com.relic.presentation.main.RelicError
import com.relic.presentation.search.DisplaySearchContract
import com.relic.presentation.search.SubredditSearchOptions
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class SubSearchVM(
    private val subRepo: SubRepository
) : RelicViewModel(), DisplaySearchContract.SubredditSearchVM {

    class Factory @Inject constructor(
        private val subRepo: SubRepository
    ) {
        fun create(): SubSearchVM {
            return SubSearchVM(subRepo)
        }
    }

    private val _subSearchErrorLiveData = MutableLiveData<RelicError?>()
    private val _subredditResultsLiveData = MutableLiveData<List<SubredditModel>>()
    private val _subscribedSubredditResultsLiveData = MutableLiveData<List<PostModel>>()

    override val subSearchErrorLiveData: LiveData<RelicError?> = _subSearchErrorLiveData
    override val subredditResultsLiveData: LiveData<List<SubredditModel>> = _subredditResultsLiveData
    override val subscribedSubredditResultsLiveData: LiveData<List<PostModel>> = _subscribedSubredditResultsLiveData

    override fun updateQuery(query: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun search(options: SubredditSearchOptions) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun retrieveMoreSubResults() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun handleException(context: CoroutineContext, e: Throwable) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}