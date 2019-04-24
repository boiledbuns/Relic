package com.relic.presentation.home.frontpage

import com.relic.data.PostRepository
import com.relic.data.SubRepository
import com.relic.network.NetworkUtil
import com.relic.presentation.displaysub.DisplaySubVM
import javax.inject.Inject

class FrontpageVM (
    subRepo: SubRepository,
    postRepo : PostRepository,
    networkUtil : NetworkUtil
): DisplaySubVM(PostRepository.PostSource.Frontpage , subRepo, postRepo, networkUtil) {

    private val TAG = "DISPLAY_SUB_VM"

    class Factory @Inject constructor(
        private val subRepo: SubRepository,
        private val postRepo : PostRepository,
        private val networkUtil : NetworkUtil
    ) {
        fun create() : FrontpageVM{
            return FrontpageVM(subRepo, postRepo, networkUtil)
        }
    }

}