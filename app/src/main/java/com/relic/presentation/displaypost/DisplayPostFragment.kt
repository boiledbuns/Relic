package com.relic.presentation.displaypost

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.*
import com.relic.MainActivity
import com.relic.R
import com.relic.dagger.DaggerVMComponent
import com.relic.dagger.modules.AuthModule
import com.relic.dagger.modules.RepoModule
import com.relic.dagger.modules.UtilModule
import com.relic.data.PostRepository
import com.relic.data.gateway.UserGatewayImpl
import com.relic.data.models.CommentModel
import com.relic.data.models.PostModel
import com.relic.network.NetworkRequestManager
import com.relic.presentation.DisplayImageFragment
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.displaypost.commentlist.CommentItemAdapter
import com.relic.presentation.displaysub.DisplaySubFragment
import com.relic.presentation.editor.EditorContract
import com.relic.presentation.editor.EditorView
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.display_post.*

class DisplayPostFragment : RelicFragment() {
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
    private var currentException : PostExceptionData? = null

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

        myToolbar = rootView.findViewById<Toolbar>(R.id.displayPostToolbar).apply {
            title = subredditName

            (activity as MainActivity).setSupportActionBar(this)

            if (enableVisitSub) setOnClickListener {
                val subFragment = DisplaySubFragment.create(subredditName)
                activity!!.supportFragmentManager.beginTransaction()
                    .replace(R.id.main_content_frame, subFragment).addToBackStack(TAG).commit()
            }

            setNavigationOnClickListener { activity?.onBackPressed() }
        }

        rootView.findViewById<RecyclerView>(R.id.postCommentRecyclerView).apply {
            commentAdapter = CommentItemAdapter(displayPostVM)
            adapter = commentAdapter
        }

        attachViewListeners()
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViewModel()

        // TODO: Testing user gateway using user "reddit"
        val userGateway = UserGatewayImpl(context!!, NetworkRequestManager(activity!!.applicationContext))
        userGateway.getUser("reddit")
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
        displayPostVM.errorLiveData.nonNull().observe(this) { handleError(it) }
        displayPostVM.refreshingLiveData.nonNull().observe(this) {
            displayPostSwipeRefresh.isRefreshing = it
            // hide all errors when refreshing
            snackbar?.dismiss()
        }
    }

    // region live data handlers

    private fun displayPost (postModel : PostModel) {
        fullPostView.setPost(postModel, displayPostVM.determineType(), displayPostVM)
    }

    private fun displayComments(commentList : List<CommentModel>) {
        // notify the adapter and set the new list
        commentAdapter.setComments(commentList)

        if (displayPostVM.errorLiveData.value is PostExceptionData.NoComments && commentList.isNotEmpty()) {
            snackbar?.dismiss()
            snackbar = null
            currentException = null
        }
    }

    private fun handleNavigation(navigationData : PostNavigationData) {
        when (navigationData) {
            is PostNavigationData.ToImage -> openImage(navigationData.imageUrl)
            is PostNavigationData.ToReply -> openPostReplyEditor(navigationData.parentFullname)
        }
    }

    private fun handleError(error : PostExceptionData) {
        // I do realize that error != exception, but still not convinced about the exception naming
        var snackbarMessage = resources.getString(R.string.unknown_error)
        var displayLength = Snackbar.LENGTH_SHORT

        var actionMessage : String? = null
        var action : () -> Unit = {}

        when (error) {
            is PostExceptionData.NoComments -> {
                // TODO show the no comment image if this sub has no comments
                // hide the loading icon if some comments have been loaded
                snackbarMessage = resources.getString(R.string.no_comments)
                displayLength = Snackbar.LENGTH_INDEFINITE
                actionMessage = resources.getString(R.string.retry)
                action = { displayPostVM.refreshData() }
            }
            is PostExceptionData.NetworkUnavailable -> {
                snackbarMessage = resources.getString(R.string.network_unavailable)
                displayLength = Snackbar.LENGTH_INDEFINITE
                actionMessage = resources.getString(R.string.refresh)
                action = { displayPostVM.refreshData() }
            }
        }

        currentException = error
        snackbar = Snackbar.make(displayPostRootView, snackbarMessage, displayLength).apply{
            actionMessage?.let {
                setAction(it) { action.invoke() }
            }
            show()
        }
    }

    // endregion live data handlers

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

    private fun openImage(imageUrl : String) {
        val displayImageFragment = DisplayImageFragment.create(imageUrl)
        activity!!.supportFragmentManager
                .beginTransaction()
                .add(R.id.main_content_frame, displayImageFragment)
                .addToBackStack(TAG)
                .commit()
    }

    private fun openPostReplyEditor(parentFullname: String) {
        val editorFragment = EditorView.create(subredditName, parentFullname, EditorContract.ParentType.COMMENT)

        // replace the current screen with the newly created fragment
        activity!!.supportFragmentManager.beginTransaction()
                .replace(R.id.main_content_frame, editorFragment).addToBackStack(TAG).commit()
    }
}
