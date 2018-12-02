package com.relic.presentation.displaypost

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.relic.R
import com.relic.dagger.DaggerVMComponent
import com.relic.dagger.modules.AuthModule
import com.relic.dagger.modules.RepoModule
import com.relic.data.gateway.UserGatewayImpl
import com.relic.data.models.CommentModel
import com.relic.data.models.PostModel
import com.relic.network.NetworkRequestManager
import com.relic.presentation.DisplayImageFragment
import com.relic.presentation.displaypost.commentlist.CommentItemAdapter
import com.relic.presentation.displaysub.DisplaySubView
import com.relic.presentation.editor.EditorContract
import com.relic.presentation.editor.EditorView
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.display_post.*

class DisplayPostFragment : Fragment() {

    companion object {
        private const val TAG = "DISPLAYPOST_VIEW"
        private const val ARG_POST_FULLNAME = "full_name"
        private const val ARG_SUB_NAME = "subreddit"

        fun create(postId : String, subreddit : String) : DisplayPostFragment {
            // create a new bundle for the post id
            val bundle = Bundle()
            bundle.putString(ARG_POST_FULLNAME, postId)
            bundle.putString(ARG_SUB_NAME, subreddit)

            return DisplayPostFragment().apply {
                arguments = bundle
            }
        }
    }
    
    private val displayPostVM : DisplayPostVM by lazy {
        ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                // construct & inject editor VM
                return DaggerVMComponent.builder()
                        .repoModule(RepoModule(context!!))
                        .authModule(AuthModule(context!!))
                        .build()
                        .getDisplayPostVM().create(subredditName, postFullName) as T
            }
        }).get(DisplayPostVM::class.java)
    }

    private lateinit var postFullName: String
    private lateinit var subredditName: String

    private lateinit var rootView : View
    private lateinit var myToolbar: Toolbar
    private lateinit var commentAdapter: CommentItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.apply {
            getString("full_name")?.let { postFullName = it }
            getString("subreddit")?.let { subredditName = it }
        }
        bindViewModel()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        rootView = inflater.inflate(R.layout.display_post, container, false).apply {

            findViewById<Toolbar>(R.id.display_post_toolbar).apply {
                myToolbar = this
                title = subredditName
                inflateMenu(R.menu.display_post_menu)

                setOnClickListener {
                    val subFragment = DisplaySubView.create(subredditName)
                    activity!!.supportFragmentManager.beginTransaction()
                        .replace(R.id.main_content_frame, subFragment).addToBackStack(TAG).commit()
                }
            }

            findViewById<RecyclerView>(R.id.postCommentRecyclerView).apply {
                commentAdapter = CommentItemAdapter(displayPostVM)
                adapter = commentAdapter
            }
        }

        attachViewListeners()
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // TODO: Testing user gateway using user "reddit"
        val userGateway = UserGatewayImpl(context!!, NetworkRequestManager(activity!!.applicationContext))
        userGateway.getUser("reddit")

        setHasOptionsMenu(true)
    }

    /**
     * subscribes the view to the data exposed by the viewmodel
     */
    private fun bindViewModel() {
        displayPostVM.postLiveData.nonNull().observe(this) { displayPost(it) }
        displayPostVM.commentListLiveData.nonNull().observe(this) { displayComments(it) }
        displayPostVM.postNavigationLiveData.nonNull().observe(this) { handleNavigation(it) }
        displayPostVM.refreshingLiveData.nonNull().observe(this) {
            displayPostSwipeRefresh.isRefreshing = it
        }
    }

    private fun displayPost (postModel : PostModel) {
        fullPostView.setPost(postModel, displayPostVM.isImage(), displayPostVM)

        if (postModel.commentCount == 0) {
            // TODO show the no comment image if this sub has no comments
            // hide the loading icon if some comments have been loaded
            Snackbar.make(displayPostRootView, "No comments for this post", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun displayComments(commentList : List<CommentModel>) {
        // notify the adapter and set the new list
        commentAdapter.setComments(commentList)
    }

    /**
     * Attaches custom scroll listeners to allow more comments to be retrieved when the recyclerview
     * is scrolled all the way to the bottom
     */
    private fun attachViewListeners() {
        rootView.apply {
            findViewById<RecyclerView>(R.id.postCommentRecyclerView)
                .addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)

                        // if recyclerview reaches bottom
                        if (!recyclerView.canScrollVertically(1)) {
                            Log.d(TAG, "Bottom reached")
                            displayPostVM.retrieveMoreComments(false)
                        }
                    }
                })

            findViewById<SwipeRefreshLayout>(R.id.displayPostSwipeRefresh)
                .setOnRefreshListener { displayPostVM.retrieveMoreComments(refresh = true) }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater?.inflate(R.menu.display_post_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        var override = true

        Log.d(TAG, "PLEASE " + item!!.itemId)
        when (item.itemId) {
            R.id.post_menu_reply -> openPostReplyEditor(postFullName)
            else -> override = super.onOptionsItemSelected(item)
        }

        return override
    }

    private fun handleNavigation(navigationData : PostNavigationData) {
        when (navigationData) {
            is PostNavigationData.ToImage -> openImage(navigationData.imageUrl)
        }
    }

    private fun openImage(imageUrl : String) {
        val displayImageFragment = DisplayImageFragment()

        Bundle().apply {
            putString("image_url", imageUrl)
            displayImageFragment.arguments = this
        }

        activity!!.supportFragmentManager
                .beginTransaction()
                .add(R.id.main_content_frame, displayImageFragment)
                .addToBackStack(TAG)
                .commit()
    }

    private fun openPostReplyEditor(fullname: String?) {
        Log.d(TAG, "reply button pressed")

        val subFrag = EditorView()

        // add the subreddit object to the bundle
        subFrag.arguments = Bundle().apply {
            putString(EditorView.SUBNAME_ARG, subredditName)
            putString(EditorView.FULLNAME_ARG, fullname)
            putInt(EditorView.PARENT_TYPE_KEY, EditorContract.VM.POST_PARENT)
        }

        // replace the current screen with the newly created fragment
        activity!!.supportFragmentManager.beginTransaction()
                .replace(R.id.main_content_frame, subFrag).addToBackStack(TAG).commit()
    }
}
