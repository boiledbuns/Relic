package com.relic.presentation.displaysub

import androidx.lifecycle.LiveData
import com.relic.data.gateway.PostGateway
import com.relic.domain.models.PostModel
import com.relic.presentation.util.MediaHelper
import com.relic.presentation.util.MediaType
import com.shopify.livedataktx.SingleLiveData
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * delegate the handles all actions related for posts
 * ex. upvoting or saving
 *
 * also exposes livedata that emits navigation events from post related actions
 */
@Singleton
class PostInteractor @Inject constructor(
  private val postGateway: PostGateway
) : DisplaySubContract.PostAdapterDelegate, CoroutineScope {

    override val coroutineContext = Dispatchers.Main + SupervisorJob() + CoroutineExceptionHandler { context, e ->
        Timber.e(e,  "caught exception")
    }

    private val _navigationLiveData = SingleLiveData<NavigationData>()
    override val navigationLiveData : LiveData<NavigationData> = _navigationLiveData

    override fun handlePostInteraction(interaction: PostInteraction) {
        val post = interaction.post
        when(interaction) {
            is PostInteraction.Visit -> visitPost(post)
            is PostInteraction.Vote -> voteOnPost(post, interaction.vote)
            is PostInteraction.Save -> savePost(post)
            is PostInteraction.PreviewUser -> previewUser(post)
            is PostInteraction.VisitLink -> visitLink(post)
            is PostInteraction.NewReply -> onNewReplyPressed(post)
        }
    }


    private fun visitPost(post: PostModel) {
        launch(Dispatchers.Main) { postGateway.visitPost(post.fullName) }
        _navigationLiveData.postValue(NavigationData.ToPost(post.fullName, post.subreddit!!))
    }

    private fun visitLink(post: PostModel) {
        post.url?.let { url ->
            val navData = when (val mediaType = MediaHelper.determineType(post)) {
                is MediaType.Image, MediaType.Gfycat -> NavigationData.ToMedia(mediaType, url)
                is MediaType.Link -> NavigationData.ToExternal(url)
                else -> null
            }

            _navigationLiveData.postValue(navData)
        }
    }

    private fun previewUser(post: PostModel) {
        _navigationLiveData.postValue(NavigationData.ToUserPreview(post.author))
    }

    private fun voteOnPost(post: PostModel, vote: Int) {
        launch(Dispatchers.Main) { postGateway.voteOnPost(post.fullName, vote) }
    }

    private fun savePost(post: PostModel) {
        launch(Dispatchers.Main) { postGateway.savePost(post.fullName, !post.saved) }
    }

    private fun onNewReplyPressed(post: PostModel) {
        _navigationLiveData.postValue(NavigationData.ToReply(post.fullName))
    }
}