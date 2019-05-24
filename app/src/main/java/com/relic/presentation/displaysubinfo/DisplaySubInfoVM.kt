package com.relic.presentation.displaysubinfo

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel

import com.relic.data.SubRepository
import com.relic.data.gateway.SubGateway
import com.relic.data.models.SubredditModel
import com.relic.presentation.displaysubinfo.DisplaySubInfoContract

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
