package com.relic.presentation.home.frontpage

import com.relic.data.PostRepository
import com.relic.data.SubRepository
import com.relic.presentation.displaysub.DisplaySubVM
import javax.inject.Inject

class FrontpageVM (
    subRepo: SubRepository,
    postRepo : PostRepository
): DisplaySubVM(PostRepository.PostSource.Frontpage , subRepo, postRepo) {

    private val TAG = "DISPLAY_SUB_VM"

    class Factory @Inject constructor(
        private val subRepo: SubRepository,
        private val postRepo : PostRepository
    ) {
        fun create() : FrontpageVM{
            return FrontpageVM(subRepo, postRepo)
        }
    }

}