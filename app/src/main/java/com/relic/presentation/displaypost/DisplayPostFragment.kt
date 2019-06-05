package com.relic.presentation.displaypost

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.*
import com.relic.R
import com.relic.dagger.DaggerVMComponent
import com.relic.dagger.modules.AuthModule
import com.relic.dagger.modules.RepoModule
import com.relic.dagger.modules.UtilModule
import com.relic.data.PostRepository
import com.relic.domain.models.CommentModel
import com.relic.domain.models.PostModel
import com.relic.presentation.media.DisplayImageFragment
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.displaypost.commentlist.CommentItemAdapter
import com.relic.presentation.displaysub.DisplaySubFragment
import com.relic.presentation.displayuser.DisplayUserPreview
import com.relic.presentation.editor.ReplyEditorFragment
import com.relic.presentation.media.DisplayGfycatFragment
import com.relic.presentation.util.MediaHelper.determineType
import com.relic.presentation.util.MediaType
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.display_post.*
import kotlinx.android.synthetic.main.full_post.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DisplayPostFragment : RelicFragment(), CoroutineScope {
    override val coroutineContext = Dispatchers.Main + SupervisorJob()

    private val displayPostVM : DisplayPostVM by lazy {
        ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                // construct & inject editor ViewModel
                return DaggerVMComponent.builder()
                        .repoModule(RepoModule(context!!))
                        .authModule(AuthModule(context!!))
                        .utilModule(UtilModule(activity!!.application))
                        .build()
                        .getDisplayPostVM().create(subredditName, postFullName, postSource) as T
            }
        }).get(DisplayPostVM::class.java)
    }

    private lateinit var postFullName: String
    private lateinit var subredditName: String
    private lateinit var postSource: PostRepository.PostSource
    private var enableVisitSub = false

    private lateinit var rootView : View
    private lateinit var myToolbar: Toolbar
    private lateinit var commentAdapter: CommentItemAdapter
    private var previousError : PostErrorData? = null

    // region lifecycle hooks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        arguments?.apply {
            getString(ARG_POST_FULLNAME)?.let { postFullName = it }
            getString(ARG_SUB_NAME)?.let { subredditName = it }
            getParcelable<PostRepository.PostSource>(ARG_POST_SOURCE)?.let { postSource = it }
            enableVisitSub = getBoolean(ARG_ENABLE_VISIT_SUB)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.display_post, container, false)

        myToolbar = rootView.findViewById(R.id.displayPostToolbar)
        (activity as AppCompatActivity).apply {
            setSupportActionBar(myToolbar)
        }

        initializeToolbar()

        rootView.findViewById<RecyclerView>(R.id.postCommentRecyclerView).apply {
            commentAdapter = CommentItemAdapter(displayPostVM)
            adapter = commentAdapter
        }

        rootView.findViewById<SwipeRefreshLayout>(R.id.displayPostSwipeRefresh).isRefreshing = true
        attachViewListeners()
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViewModel()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)

        menu?.clear()
        inflater?.inflate(R.menu.display_post_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        var override = true

        when (item?.itemId) {
            R.id.post_menu_reply -> openPostReplyEditor(postFullName)
            else -> override = super.onOptionsItemSelected(item)
        }

        return override
    }

    // endregion lifecycle hooks

    private fun bindViewModel() {
        displayPostVM.postLiveData.nonNull().observe(this) { displayPost(it) }
        displayPostVM.commentListLiveData.nonNull().observe(this) { displayComments(it) }
        displayPostVM.postNavigationLiveData.nonNull().observe(this) { handleNavigation(it) }
        displayPostVM.errorLiveData.observe(this) { handleError(it) }
    }

    // region live data handlers

    private fun displayPost (postModel : PostModel) {
        fullPostView.setPost(postModel, determineType(postModel), displayPostVM)
        postAuthorView.setOnClickListener { displayPostVM.onUserPressed(postModel) }
    }

    private fun displayComments(commentList : List<CommentModel>) {
        launch(Dispatchers.Main) {
            // notify the adapter and set the new list
            commentAdapter.setComments(commentList) {
//                displayPostSwipeRefresh.isRefreshing = false
            }
            displayPostSwipeRefresh.isRefreshing = false
        }
    }

    private fun handleNavigation(navigationData : PostNavigationData) {
        when (navigationData) {
            is PostNavigationData.ToMedia -> openMedia(navigationData)
            is PostNavigationData.ToReply -> openPostReplyEditor(navigationData.parentFullname)
            is PostNavigationData.ToURL -> {
                Intent(Intent.ACTION_VIEW).apply{
                    data = Uri.parse(navigationData.url)
                    startActivity(this)
                }
            }
            is PostNavigationData.ToUserPreview -> {
                DisplayUserPreview.create(navigationData.username)
                    .show(fragmentManager, TAG)
            }
        }
    }

    private fun handleError(error : PostErrorData?) {
        if (previousError != error) {
            displayPostSwipeRefresh.isRefreshing = false

            // default details for unhandled exceptions to be displayed
            var snackbarMessage = resources.getString(R.string.unknown_error)
            var displayLength = Snackbar.LENGTH_SHORT
            var actionMessage: String? = null
            var action: () -> Unit = {}

            when (error) {
                is PostErrorData.NetworkUnavailable -> {
                    snackbarMessage = resources.getString(R.string.network_unavailable)
                    displayLength = Snackbar.LENGTH_INDEFINITE
                    actionMessage = resources.getString(R.string.refresh)
                    action = { displayPostVM.refreshData() }
                }
            }

            snackbar = Snackbar.make(displayPostRootView, snackbarMessage, displayLength).apply {
                actionMessage?.let {
                    setAction(it) { action.invoke() }
                }
                show()
            }
        }
    }

    // endregion live data handlers

    private fun initializeToolbar() {
        val pActivity = (activity as AppCompatActivity)

        pActivity.supportActionBar?.apply {
            title = subredditName

            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

        myToolbar.apply {
            setNavigationOnClickListener { activity?.onBackPressed() }
            if (enableVisitSub) setOnClickListener {
                val subFragment = DisplaySubFragment.create(subredditName)
                activity!!.supportFragmentManager.beginTransaction()
                    .replace(R.id.main_content_frame, subFragment).addToBackStack(TAG).commit()
            }
        }
    }

    /**
     * Attaches custom scroll listeners to allow more comments to be retrieved when the recycler
     * view is scrolled all the way to the bottom
     */
    private fun attachViewListeners() {
        rootView.findViewById<RecyclerView>(R.id.postCommentRecyclerView)
            .addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)

                    // if recycler view reaches bottom
                    if (!recyclerView.canScrollVertically(1)) {
                        Log.d(TAG, "Bottom reached")
                        displayPostVM.retrieveMoreComments(false)
                    }
                }
            })

        rootView.findViewById<SwipeRefreshLayout>(R.id.displayPostSwipeRefresh).apply {
            setOnRefreshListener {
                displayPostVM.refreshData()
            }
        }
    }

    private fun openMedia(navMediaData : PostNavigationData.ToMedia) {
        val displayFragment = when (navMediaData.mediaType)  {
            MediaType.Gfycat -> DisplayGfycatFragment.create(navMediaData.mediaUrl)
            else -> DisplayImageFragment.create(navMediaData.mediaUrl)
        }
        activity!!.supportFragmentManager
                .beginTransaction()
                .add(R.id.main_content_frame, displayFragment)
                .addToBackStack(TAG)
                .commit()
    }

    private fun openPostReplyEditor(parentFullname: String) {
        // this option is for replying to parent
        // Should also allow user to do it inline, but that can be saved for a later task
        val editorFragment = ReplyEditorFragment.create(parentFullname, true)

        // replace the current screen with the newly created fragment
        activity!!.supportFragmentManager.beginTransaction()
                .replace(R.id.main_content_frame, editorFragment).addToBackStack(TAG).commit()
    }

    companion object {
        private const val TAG = "DISPLAYPOST_VIEW"
        private const val ARG_POST_FULLNAME = "full_name"
        private const val ARG_SUB_NAME = "subreddit"
        private const val ARG_POST_SOURCE = "post_source"
        private const val ARG_ENABLE_VISIT_SUB = "enable_visit_sub"

        /**
         * @param enableVisitSub used to allow onClicks to subreddit. Should only be enabled when
         * visiting post from different source than its sub (ie frontpage, all, etc) to prevent
         * continuously chaining open subreddit actions
         */
        fun create(postId : String, subreddit : String, postSource: PostRepository.PostSource, enableVisitSub : Boolean = false) : DisplayPostFragment {
            // create a new bundle for the post id
            val bundle = Bundle().apply {
                putString(ARG_POST_FULLNAME, postId)
                putString(ARG_SUB_NAME, subreddit)
                putBoolean(ARG_ENABLE_VISIT_SUB, enableVisitSub)
                putParcelable(ARG_POST_SOURCE, postSource)
            }

            return DisplayPostFragment().apply {
                arguments = bundle
            }
        }
    }
}
