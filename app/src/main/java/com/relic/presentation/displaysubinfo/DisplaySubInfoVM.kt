package com.relic.presentation.displaysubinfo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import com.relic.data.SubRepository
import com.relic.data.gateway.SubGateway
import com.relic.domain.models.SubredditModel

class DisplaySubInfoVM : ViewModel(), DisplaySubInfoContract.ViewModel {
    private var subRepo: SubRepository? = null
    private var subGateway: SubGateway? = null
    private var subredditModel: LiveData<SubredditModel>? = null

    private var subredditName: String? = null
    private val isSubbed: LiveData<Boolean>? = null

    override fun initialize(subName: String, subRepo: SubRepository) {
        this.subRepo = subRepo
        this.subGateway = subRepo.getSubGateway()

        subredditName = subName

        fetchValues()
    }


    private fun fetchValues() {
        subredditModel = subRepo!!.getSingleSub(subredditName!!)
    }

    override fun getSubreddit(): LiveData<SubredditModel> {
        return subRepo!!.getSingleSub(subredditName!!)
    }

    override fun retrieveSubreddit() {
        //    subRepo.retrieveSingleSub("getnarwhal");
    }

    override fun subscribe(): LiveData<Boolean> {
        //return subGateway.subscribe(subredditName);
        return MutableLiveData()
    }

    override fun unsubscribe(): LiveData<Boolean> {
        //return subGateway.unsubscribe(subredditName);
        return MutableLiveData()

    }
}
