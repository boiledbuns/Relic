package com.relic.interactor

import androidx.lifecycle.LiveData
import com.relic.data.PostSource
import com.relic.domain.models.CommentModel
import com.relic.domain.models.PostModel
import com.relic.domain.models.SubredditModel
import com.relic.presentation.displaysub.NavigationData

// delegates for handling domain layer interactions
interface Contract {
    interface CommentAdapterDelegate {
        fun interact(comment: CommentModel, interaction: CommentInteraction)
    }

    interface PostAdapterDelegate {
        val navigationLiveData : LiveData<NavigationData>
        fun interact(post : PostModel, interaction: PostInteraction)
    }

    interface SubAdapterDelegate {
        val navigationLiveData: LiveData<NavigationData>
        fun interact(postSource: PostSource, subInteraction: SubInteraction)
    }
}

sealed class CommentInteraction {
    object Upvote: CommentInteraction()
    object Downvote: CommentInteraction()
    data class NewReply(val text: String): CommentInteraction()
    object PreviewUser: CommentInteraction()
    object ExpandReplies: CommentInteraction()
    object Visit: CommentInteraction()
}

sealed class PostInteraction {
    object Visit: PostInteraction()
    object Upvote: PostInteraction()
    object Downvote: PostInteraction()
    object Save: PostInteraction()
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