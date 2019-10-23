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

    override fun visitPost(postFullname : String, subreddit : String) {
        launch(Dispatchers.Main) { postGateway.visitPost(postFullname) }
        _navigationLiveData.postValue(NavigationData.ToPost(postFullname, subreddit))
    }

    override fun onLinkPressed(postModel: PostModel) {
        postModel.url?.let { url ->
            val navData = when (val mediaType = MediaHelper.determineType(postModel)) {
                is MediaType.Image, MediaType.Gfycat -> NavigationData.ToMedia(mediaType, url)
                is MediaType.Link -> NavigationData.ToExternal(url)
                else -> null
            }

            _navigationLiveData.postValue(navData)
        }
    }

    override fun previewUser(username: String) {
        _navigationLiveData.postValue(NavigationData.ToUserPreview(username))
    }

    override fun voteOnPost(postFullname: String, voteValue: Int) {
        launch(Dispatchers.Main) { postGateway.voteOnPost(postFullname, voteValue) }
    }

    override fun savePost(postFullname: String, save: Boolean) {
        launch(Dispatchers.Main) { postGateway.savePost(postFullname, save) }
    }

    override fun onNewReplyPressed(postFullname: String) {
        _navigationLiveData.postValue(NavigationData.ToReply(postFullname))
    }
}