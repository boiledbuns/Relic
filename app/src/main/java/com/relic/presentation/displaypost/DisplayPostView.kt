package com.relic.presentation.displaypost

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.BindingAdapter
import android.databinding.DataBindingUtil
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
import android.widget.ImageView
import com.relic.R
import com.relic.dagger.DaggerVMComponent
import com.relic.data.RepoModule
import com.relic.data.gateway.UserGatewayImpl
import com.relic.data.models.PostModel
import com.relic.databinding.DisplayPostBinding
import com.relic.presentation.adapter.CommentAdapter
import com.relic.presentation.editor.EditorContract
import com.relic.presentation.editor.EditorView
import com.shopify.livedataktx.observe
import com.squareup.picasso.Picasso

import java.util.Arrays

class DisplayPostView : Fragment() {

    companion object {
        private val picEndings = Arrays.asList("jpg", "png")

        /**
         * Bind this method to the preview image to automatically load the image into it
         * @param imgView imageview to hold the preview image
         * @param previewThumbnail url of the thumbnail image
         * @param previewFullImage url of the full image
         */
        @JvmStatic
        @BindingAdapter("bind:previewThumbnail", "bind:previewFullImage")
        fun LoadPreviewImage(
                imgView: ImageView,
                previewThumbnail: String?,
                previewFullImage: String?
        ) {
            //    String linkUrl = postModel.getDomain();
            //    boolean notEmpty = !linkUrl.isEmpty();
            //    List <String> validUrls = Arrays.asList("self", "i.re");
            //
            //    if (notEmpty && !validUrls.contains(linkUrl.substring(0, 4))) {
            //      // loads the card image
            //      Log.d(TAG, linkUrl.substring(0, 4) + "");
            //      Picasso.get().load(postModel.getThumbnail()).fit().centerCrop().into(displayPostBinding.displayPostCardThumbnail);
            //    }
            //    else {
            //      String fullUrl = postModel.getUrl();
            //      // load the full image for the image
            //      if (picEndings.contains(fullUrl.substring(fullUrl.length() - 3))) {
            //        try {
            //          Picasso.get().load(fullUrl).fit().centerCrop().into(displayPostBinding.displaypostPreview);
            //        }
            //        catch (Error error) {
            //          Log.d("DISPLAYPOST_VIEW", "Issue loading image " + error.toString());
            //        }
            //      }
            //    }
        }
    }

    private val TAG = "DISPLAYPOST_VIEW"

    private val displayPostVM : DisplayPostVM by lazy {
        initializeVM()
    }

    private fun initializeVM() : DisplayPostVM {
        return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                // construct & inject editor VM
                return DaggerVMComponent.builder()
                        .repoModule(RepoModule(context!!))
                        .build()
                        .getDisplayPostVM().create(subredditName!!, postFullname!!) as T
            }
        }).get(DisplayPostVM::class.java)
    }

    private lateinit var displayPostBinding: DisplayPostBinding

    private var contentView: View? = null
    private var myToolbar: Toolbar? = null
    private var commentAdapter: CommentAdapter? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null

    private lateinit var postFullname: String
    private lateinit var subredditName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.apply {
            getString("full_name")?.let {
                postFullname = it
            }
            getString("subreddit")?.let {
                subredditName = it
            }
        }

        // parse the full name of the post to be displayed
        Log.d(TAG, "Post fullname : " + postFullname!!)
        bindViewModel()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        displayPostBinding = DataBindingUtil.inflate(inflater, R.layout.display_post, container, false)

        contentView = displayPostBinding.root

        displayPostBinding.root.findViewById <Toolbar> (R.id.display_post_toolbar).apply {
            myToolbar = this
            title = subredditName
            inflateMenu(R.menu.display_post_menu)
        }

        commentAdapter = CommentAdapter(displayPostVM)
        displayPostBinding.displayCommentsRecyclerview.adapter = commentAdapter
        displayPostBinding.displayCommentsRecyclerview.itemAnimator = null

        // get a reference to the swipe refresh layout and attach the scroll listeners
        swipeRefreshLayout = displayPostBinding.root.findViewById(R.id.postitem_swiperefresh)
        attachScrollListeners()

        initializeOnClicks()

        return displayPostBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // TODO: Testing user gateway using user "reddit"
        val userGateway = UserGatewayImpl(context!!)
        userGateway.getUser("reddit")

        setHasOptionsMenu(true)
    }

    /**
     * subscribes the view to the data exposed by the viewmodel
     */
    private fun bindViewModel() {
        // Observe the post exposed by the VM
        displayPostVM.post.observe(this) { postModel ->
            if (postModel != null) {
                displayPostBinding.postItem = postModel

                // load the image or link card based on the type of link
                loadLinkPreview(postModel)

                if (postModel.commentCount == 0) {
                    // hide the loading icon if some comments have been loaded
                    displayPostBinding!!.displayPostLoadingComments.visibility = View.GONE

                    // TODO show the no comment image if this sub has no comments
                    Snackbar.make(
                            displayPostBinding!!.root,
                            "No comments for this post",
                            Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // Observe the list of comments exposed by the VM
        displayPostVM.commentList.observe(this) { commentModels ->
            // notify the adapter and set the new list
            if (commentModels != null) {
                commentAdapter!!.setComments(commentModels)
                Log.d(TAG, "Comments " + commentModels.size)
                swipeRefreshLayout!!.isRefreshing = false

                // hide the loading icon if some comments have been loaded
                displayPostBinding.displayPostLoadingComments.visibility = View.GONE
            }
        }
    }

    /**
     * Attaches custom scroll listeners to allow more comments to be retrieved when the recyclerview
     * is scrolled all the way to the bottom
     */
    private fun attachScrollListeners() {
        displayPostBinding.displayCommentsRecyclerview.addOnScrollListener(object :
                RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                // if recyclerview reaches bottom
                if (!recyclerView.canScrollVertically(1)) {
                    Log.d(TAG, "Bottom reached")
                    displayPostVM.retrieveMoreComments(false)
                }
            }
        })

        swipeRefreshLayout!!.setOnRefreshListener { displayPostVM!!.refresh() }
    }

    private fun loadLinkPreview(postModel: PostModel) {
        val linkUrl = postModel.domain
        val notEmpty = !linkUrl.isEmpty()
        var isImage = false
        val validUrls = Arrays.asList("self", "i.re")
        Log.d(TAG, linkUrl.substring(0, 4))

        if (notEmpty && !validUrls.contains(linkUrl.substring(0, 4))) {
            // loads the card image
            Picasso.get().load(postModel.thumbnail).fit().centerCrop()
                    .into(displayPostBinding!!.displayPostCardThumbnail)
        } else {
            val fullUrl = postModel.url
            // load the full image for the image
            if (picEndings.contains(fullUrl.substring(fullUrl.length - 3))) {
                try {
                    Picasso.get().load(fullUrl).fit().centerCrop()
                            .into(displayPostBinding!!.displaypostPreview)
                    isImage = true
                } catch (error: Error) {
                    Log.d("DISPLAYPOST_VIEW", "Issue loading image " + error.toString())
                }
            }
        }

        displayPostBinding.isImage = isImage
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater!!.inflate(R.menu.display_post_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        var override = true

        Log.d(TAG, "PLEASE " + item!!.itemId)
        when (item.itemId) {
            R.id.post_menu_reply -> openPostReplyEditor(postFullname)
            else -> override = super.onOptionsItemSelected(item)
        }

        return override
    }

    private fun openPostReplyEditor(fullname: String?) {
        Log.d(TAG, "reply button pressed")

        // add the subreddit object to the bundle
        val bundle = Bundle()
        bundle.putString(EditorView.SUBNAME_ARG, subredditName)
        bundle.putString(EditorView.FULLNAME_ARG, fullname)
        bundle.putInt(EditorView.PARENT_TYPE_KEY, EditorContract.VM.POST_PARENT)

        val subFrag = EditorView()
        subFrag.arguments = bundle

        // replace the current screen with the newly created fragment
        activity!!.supportFragmentManager.beginTransaction()
                .replace(R.id.main_content_frame, subFrag).addToBackStack(TAG).commit()
    }

    /**
     * initialize main onclicks for the post
     */
    private fun initializeOnClicks() {
        contentView!!.findViewById<View>(R.id.display_post_reply)
                .setOnClickListener { view: View -> openPostReplyEditor(postFullname) }
    }
}
