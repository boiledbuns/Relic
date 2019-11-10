package com.relic.presentation.home.frontpage

import com.relic.data.ListingRepository
import com.relic.data.PostRepository
import com.relic.data.PostSource
import com.relic.data.SubRepository
import com.relic.interactor.Contract
import com.relic.network.NetworkUtil
import com.relic.presentation.displaysub.DisplaySubVM
import javax.inject.Inject

class FrontpageVM (
    subRepo: SubRepository,
    postRepo : PostRepository,
    private val postInteractor : Contract.PostAdapterDelegate,
    private val listingRepo : ListingRepository,
    networkUtil : NetworkUtil
): DisplaySubVM(PostSource.Frontpage, subRepo, postRepo, postInteractor, listingRepo, networkUtil) {

    class Factory @Inject constructor(
        private val subRepo: SubRepository,
        private val postRepo : PostRepository,
        private val postInteractor : Contract.PostAdapterDelegate,
        private val listingRepo : ListingRepository,
        private val networkUtil : NetworkUtil
    ) {
        fun create() : FrontpageVM{
            return FrontpageVM(subRepo, postRepo, postInteractor, listingRepo, networkUtil)
        }
    }

}