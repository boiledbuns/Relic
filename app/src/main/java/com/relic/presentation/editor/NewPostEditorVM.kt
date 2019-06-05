package com.relic.presentation.editor

import com.relic.data.CommentRepository
import com.relic.data.PostRepository
import com.relic.presentation.base.RelicViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class NewPostEditorVM constructor(
    private val postRepo : PostRepository,
    private val subreddit : String
): EditorContract.NewPostEditor, RelicViewModel() {

    class Factory @Inject constructor(
            private val postRepo : PostRepository,
            private val commentRepo: CommentRepository
    ) {
        fun create (subreddit: String) : NewPostEditorVM {
            return NewPostEditorVM(postRepo, subreddit)
        }
    }

    private var currentTitle = ""
    private var currentBody = ""

    init {
        launch (Dispatchers.IO) {
            // check if existing draft exists
            val existingDraft = postRepo.loadDraft(subreddit)

            if (existingDraft != null) {
                // TODO tell user
            }
        }
    }

    override fun submit() {
        launch(Dispatchers.Main) {
            postRepo.postPost(
                PostRepository.PostDraft(title = currentTitle, body = currentBody, subreddit = subreddit),
                PostRepository.PostType.Self()
            )
        }
    }

    override fun saveChanges() {
        launch(Dispatchers.Main) {
            // create a new post item to save
            postRepo.saveDraft(
                PostRepository.PostDraft(title = currentTitle, body = currentBody, subreddit = subreddit)
            )
        }
    }

    override fun onTitleChanged(newTitle : String) {
        currentTitle = newTitle
    }

    override fun onBodyChanged(newBody : String) {
        currentBody = newBody
    }

    override fun handleException(context: CoroutineContext, e: Throwable) {
        // TODO
    }
}