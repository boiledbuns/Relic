package com.relic.interactor

import androidx.lifecycle.LiveData
import com.relic.data.PostSource
import com.relic.domain.models.CommentModel
import com.relic.domain.models.PostModel
import com.relic.domain.models.SubredditModel
import com.relic.presentation.displaysub.NavigationData
import com.relic.presentation.util.RelicEvent

// delegates for handling domain layer interactions
interface Contract {
    interface CommentAdapterDelegate {
        val navigationLiveData: LiveData<RelicEvent<NavigationData>>
        fun interact(comment: CommentModel, interaction: CommentInteraction)
    }

    interface PostAdapterDelegate {
        val navigationLiveData: LiveData<RelicEvent<NavigationData>>
        fun interact(post: PostModel, interaction: PostInteraction)
    }

    interface SubAdapterDelegate {
        val navigationLiveData: LiveData<RelicEvent<NavigationData>>
        fun interact(postSource: PostSource, subInteraction: SubInteraction)
    }

    interface UserAdapterDelegate {
        val navigationLiveData: LiveData<RelicEvent<NavigationData>>
        fun interact(interaction: UserInteraction)
    }
}

sealed class CommentInteraction {
    object Upvote : CommentInteraction()
    object Downvote : CommentInteraction()
    data class NewReply(val text: String) : CommentInteraction()
    object PreviewUser : CommentInteraction()
    object ExpandReplies : CommentInteraction()
    object Visit : CommentInteraction()
}

sealed class PostInteraction {
    object Visit : PostInteraction()
    object Upvote : PostInteraction()
    object Downvote : PostInteraction()
    object Save : PostInteraction()
    object PreviewUser : PostInteraction()
    object VisitLink : PostInteraction()
    object NewReply : PostInteraction()
}

sealed class SubInteraction {
    object Visit : SubInteraction()
    object Preview : SubInteraction()
    data class Subscribe(
        val subredditModel: SubredditModel
    ) : SubInteraction()
}

sealed class UserInteraction {
    data class ViewUser(val username: String) : UserInteraction()
}