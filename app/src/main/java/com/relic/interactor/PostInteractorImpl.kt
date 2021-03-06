package com.relic.interactor

import androidx.lifecycle.LiveData
import com.relic.data.gateway.PostGateway
import com.relic.domain.models.PostModel
import com.relic.presentation.displaysub.NavigationData
import com.relic.presentation.util.MediaHelper
import com.relic.presentation.util.MediaType
import com.relic.presentation.util.RelicEvent
import com.shopify.livedataktx.SingleLiveData
import kotlinx.coroutines.*
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
class PostInteractorImpl @Inject constructor(
    private val postGateway: PostGateway
) : Contract.PostAdapterDelegate, CoroutineScope {

    override val coroutineContext = Dispatchers.Main + SupervisorJob() + CoroutineExceptionHandler { context, e ->
        Timber.e(e, "caught exception")
    }

    private val _navigationLiveData = SingleLiveData<RelicEvent<NavigationData>>()
    override val navigationLiveData: LiveData<RelicEvent<NavigationData>> = _navigationLiveData

    override fun interact(post: PostModel, interaction: PostInteraction) {
        when (interaction) {
            is PostInteraction.Visit -> visitPost(post)
            is PostInteraction.Upvote -> voteOnPost(post, 1)
            is PostInteraction.Downvote -> voteOnPost(post, -1)
            is PostInteraction.Save -> savePost(post)
            is PostInteraction.PreviewUser -> previewUser(post)
            is PostInteraction.VisitLink -> visitLink(post)
            is PostInteraction.NewReply -> onNewReplyPressed(post)
        }
    }


    private fun visitPost(post: PostModel) {
        launch(Dispatchers.Main) { postGateway.visitPost(post.fullName) }
        post.visited = true
        _navigationLiveData.postValue(RelicEvent(NavigationData.ToPost(post.fullName, post.subreddit!!)))
    }

    private fun visitLink(post: PostModel) {
        post.url?.let { url ->
            val navData = when (val mediaType = MediaHelper.determineType(post)) {
                is MediaType.Image -> NavigationData.ToImage(url)
                is MediaType.Link -> NavigationData.ToExternal(url)
                is MediaType.VReddit -> {
                    NavigationData.ToMedia(mediaType)
                }
                is MediaType.Gfycat -> NavigationData.ToMedia(mediaType)
                null -> null
                else -> NavigationData.ToExternal(url)
            }

            navData?.let { _navigationLiveData.postValue(RelicEvent(it)) }
        }

        post.visited = true
    }

    private fun previewUser(post: PostModel) {
        _navigationLiveData.postValue(RelicEvent(NavigationData.ToUserPreview(post.author)))
    }

    private fun voteOnPost(post: PostModel, vote: Int) {
        launch(Dispatchers.Main) { postGateway.voteOnPost(post.fullName, vote) }
        post.userUpvoted = when (post.userUpvoted) {
            1 -> if (vote == 1) 0 else -1
            -1 -> if (vote == -1) 0 else 1
            else -> vote
        }
    }

    private fun savePost(post: PostModel) {
        launch(Dispatchers.Main) { postGateway.savePost(post.fullName, !post.saved) }
        post.saved = !post.saved
    }

    private fun onNewReplyPressed(post: PostModel) {
        _navigationLiveData.postValue(RelicEvent(NavigationData.ToReply(post.fullName)))
    }
}