package com.relic.presentation.displaypost;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.List;

public class DisplayPostView extends Fragment {
  private final String TAG = "DISPLAYPOST_VIEW";

  private DisplayPostContract.ViewModel displayPostVM;
  private DisplayPostBinding displayPostBinding;
  private Toolbar myToolbar;
  private CommentAdapter commentAdapter;
  private SwipeRefreshLayout swipeRefreshLayout;

  private String postFullname;
  private String subredditName;
  private static List picEndings = Arrays.asList("jpg", "png");

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }


  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    displayPostBinding = DataBindingUtil
        .inflate(inflater, R.layout.display_post, container, false);

    myToolbar = displayPostBinding.getRoot().findViewById(R.id.display_post_toolbar);

    try {
      // parse the full name of the post to be displayed
      postFullname = getArguments().getString("full_name");
      subredditName = getArguments().getString("subreddit");
      Log.d(TAG, "Post fullname : " + postFullname);
    }
    catch (Exception e) {
      Toast.makeText(getContext(), "Fragment not loaded properly!", Toast.LENGTH_SHORT).show();
    }

    if (subredditName != null) {
      myToolbar.setTitle(subredditName);
    }

    commentAdapter = new CommentAdapter();
    displayPostBinding.displayCommentsRecyclerview.setAdapter(commentAdapter);

    // get a reference to the swipe refresh layout and attach the scroll listeners
    swipeRefreshLayout = displayPostBinding.getRoot().findViewById(R.id.postitem_swiperefresh);
    attachScrollListeners();

    return displayPostBinding.getRoot();
  }


  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    // create the VM and initialize it with injected dependencies
    displayPostVM = ViewModelProviders.of(this).get(DisplayPostVM.class);
    displayPostVM.init(new ListingRepositoryImpl(getContext()), new PostRepositoryImpl(getContext()),
        new CommentRepositoryImpl(getContext()), subredditName, postFullname);

    subscribeToVM();

    // Testing user gateway using user "reddit"
    UserGateway userGateway = new UserGatewayImpl(getContext());
    userGateway.getUser("reddit");
  }


  /**
   * subscribes the view to the data exposed by the viewmodel
   */
  private void subscribeToVM() {
    // Observe the post exposed by the VM
    displayPostVM.getPost().observe(this, new Observer<PostModel>() {
      @Override
      public void onChanged(@Nullable PostModel postModel) {
        if (postModel != null) {
          displayPostBinding.setPostItem(postModel);
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
        }
      }
    });
  }


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
        displayPostVM.retrieveMoreComments(true);
      }
    });
  }


  /**
   * Bind this method to the preview image to automatically load the image into it
   * @param imgView imageview to hold the preview image
   * @param previewThumbnail url of the thumbnail image
   * @param previewFullImage url of the full image
   */
  @BindingAdapter({"bind:previewThumbnail", "bind:previewFullImage"})
  public static void LoadPreviewImage(ImageView imgView, String previewThumbnail, String previewFullImage) {
    Log.d("DISPLAYPOST_VIEW", "THUMBNAIL URL = " + previewFullImage);
    String useUrl = previewFullImage;

    // use the thumbnail image url if the full url is not an image
    if (previewFullImage == null || !picEndings.contains(previewFullImage.substring(previewFullImage.length() - 3))) {
      useUrl = previewThumbnail;
    }

    try {
      // does not load image if the banner img string is empty
      Picasso.get().load(useUrl).fit().centerCrop().into(imgView);
    }
    catch (Error e) {
      Log.d("DISPLAYPOST_VIEW", "Issue loading image " + e.toString());
    }
  }

}
