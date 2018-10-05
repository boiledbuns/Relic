package com.relic.presentation.displaypost;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.relic.R;
import com.relic.data.CommentRepositoryImpl;
import com.relic.data.ListingRepositoryImpl;
import com.relic.data.PostRepositoryImpl;
import com.relic.data.gateway.UserGateway;
import com.relic.data.gateway.UserGatewayImpl;
import com.relic.data.models.CommentModel;
import com.relic.data.models.PostModel;
import com.relic.databinding.DisplayPostBinding;
import com.relic.presentation.adapter.CommentAdapter;
import com.relic.presentation.editor.EditorContract;
import com.relic.presentation.editor.EditorView;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.List;

public class DisplayPostView extends Fragment {
  private final String TAG = "DISPLAYPOST_VIEW";

  private DisplayPostContract.ViewModel displayPostVM;
  private DisplayPostBinding displayPostBinding;

  private View contentView;
  private Toolbar myToolbar;
  private CommentAdapter commentAdapter;
  private SwipeRefreshLayout swipeRefreshLayout;

  private String postFullname;
  private String subredditName;
  private static List picEndings = Arrays.asList("jpg", "png");

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    try {
      // parse the full name of the post to be displayed
      postFullname = getArguments().getString("full_name");
      subredditName = getArguments().getString("subreddit");
      Log.d(TAG, "Post fullname : " + postFullname);
    }
    catch (Exception e) {
      Toast.makeText(getContext(), "Fragment not loaded properly!", Toast.LENGTH_SHORT).show();
    }

    // create the VM and initialize it with injected dependencies
    displayPostVM = ViewModelProviders.of(this).get(DisplayPostVM.class);
    displayPostVM.init(new ListingRepositoryImpl(getContext()), new PostRepositoryImpl(getContext()),
        new CommentRepositoryImpl(getContext()), subredditName, postFullname);
  }


  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    displayPostBinding = DataBindingUtil
        .inflate(inflater, R.layout.display_post, container, false);

    contentView = displayPostBinding.getRoot();

    myToolbar = displayPostBinding.getRoot().findViewById(R.id.display_post_toolbar);

    if (subredditName != null) {
      myToolbar.setTitle(subredditName);
      // force menu to display
      myToolbar.inflateMenu(R.menu.display_post_menu);
    }

    commentAdapter = new CommentAdapter(displayPostVM);
    displayPostBinding.displayCommentsRecyclerview.setAdapter(commentAdapter);
    displayPostBinding.displayCommentsRecyclerview.setItemAnimator(null);

    // get a reference to the swipe refresh layout and attach the scroll listeners
    swipeRefreshLayout = displayPostBinding.getRoot().findViewById(R.id.postitem_swiperefresh);
    attachScrollListeners();

    initializeOnClicks();

    return displayPostBinding.getRoot();
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    bindViewModel();

    // TODO: Testing user gateway using user "reddit"
    UserGateway userGateway = new UserGatewayImpl(getContext());
    userGateway.getUser("reddit");

    setHasOptionsMenu(true);
  }


  /**
   * subscribes the view to the data exposed by the viewmodel
   */
  private void bindViewModel() {
    // Observe the post exposed by the VM
    displayPostVM.getPost().observe(this, new Observer<PostModel>() {
      @Override
      public void onChanged(@Nullable PostModel postModel) {
        if (postModel != null) {
          displayPostBinding.setPostItem(postModel);

          // load the image or link card based on the type of link
          loadLinkPreview(postModel);

          if (postModel.getCommentCount() == 0) {
            // hide the loading icon if some comments have been loaded
            displayPostBinding.displayPostLoadingComments.setVisibility(View.GONE);

            // TODO show the no comment image if this sub has no comments
            Snackbar.make(displayPostBinding.getRoot(), "No comments for this post", Snackbar.LENGTH_SHORT).show();
          }
        }
      }
    });

    // Observe the list of comments exposed by the VM
    displayPostVM.getCommentList().observe(this, new Observer<List<CommentModel>>() {
      @Override
      public void onChanged(@Nullable List<CommentModel> commentModels) {
        // notify the adapter and set the new list
        if (commentModels != null) {
          commentAdapter.setComments(commentModels);
          Log.d(TAG, "Comments " + commentModels.size());
          swipeRefreshLayout.setRefreshing(false);

          // hide the loading icon if some comments have been loaded
          displayPostBinding.displayPostLoadingComments.setVisibility(View.GONE);
        }
      }
    });
  }


  /**
   * Attaches custom scroll listeners to allow more comments to be retrieved when the recyclerview
   * is scrolled all the way to the bottom
   */
  private void attachScrollListeners() {
    displayPostBinding.displayCommentsRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);

        // if recyclerview reaches bottom
        if (!recyclerView.canScrollVertically(1)) {
          Log.d(TAG, "Bottom reached");
          displayPostVM.retrieveMoreComments(false);
        }
      }
    });

    swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        displayPostVM.refresh();
      }
    });
  }

  private void loadLinkPreview(PostModel postModel) {
    String linkUrl = postModel.getDomain();
    boolean notEmpty = !linkUrl.isEmpty();
    boolean isImage = false;
    List <String> validUrls = Arrays.asList("self", "i.re");
    Log.d(TAG, linkUrl.substring(0, 4));

    if (notEmpty && !validUrls.contains(linkUrl.substring(0, 4))) {
      // loads the card image
      Picasso.get().load(postModel.getThumbnail()).fit().centerCrop().into(displayPostBinding.displayPostCardThumbnail);
    }
    else {
      String fullUrl = postModel.getUrl();
      // load the full image for the image
      if (picEndings.contains(fullUrl.substring(fullUrl.length() - 3))) {
        try {
          Picasso.get().load(fullUrl).fit().centerCrop().into(displayPostBinding.displaypostPreview);
          isImage = true;
        }
        catch (Error error) {
          Log.d("DISPLAYPOST_VIEW", "Issue loading image " + error.toString());
        }
      }
    }

    displayPostBinding.setIsImage(isImage);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);

    inflater.inflate(R.menu.display_post_menu, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    boolean override = true;

    Log.d(TAG,  "PLEASE " + item.getItemId());
    switch (item.getItemId()) {
      case R.id.post_menu_reply : openPostReplyEditor(postFullname); break;
      default: override = super.onOptionsItemSelected(item);
    }

    return override;
  }

  private void openPostReplyEditor(String fullname) {
    Log.d(TAG, "reply button pressed");

    // add the subreddit object to the bundle
    Bundle bundle = new Bundle();
    bundle.putString(EditorView.SUBNAME_ARG, subredditName);
    bundle.putString(EditorView.FULLNAME_ARG, fullname);
    bundle.putInt(EditorView.PARENT_TYPE_KEY, EditorContract.VM.POST_PARENT);

    EditorView subFrag = new EditorView();
    subFrag.setArguments(bundle);

    // replace the current screen with the newly created fragment
    getActivity().getSupportFragmentManager().beginTransaction()
        .replace(R.id.main_content_frame, subFrag).addToBackStack(TAG).commit();
  }

  /**
   * Bind this method to the preview image to automatically load the image into it
   * @param imgView imageview to hold the preview image
   * @param previewThumbnail url of the thumbnail image
   * @param previewFullImage url of the full image
   */
  @BindingAdapter({"bind:previewThumbnail", "bind:previewFullImage"})
  public static void LoadPreviewImage(ImageView imgView, String previewThumbnail, String previewFullImage) {
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


  /**
   * initialize main onclicks for the post
   */
  private void initializeOnClicks() {
    contentView.findViewById(R.id.display_post_reply).setOnClickListener((View view) -> {
      openPostReplyEditor(postFullname);
    });
  }
}
