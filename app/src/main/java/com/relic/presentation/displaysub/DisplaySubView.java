package com.relic.presentation.displaysub;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.relic.R;
import com.relic.data.PostRepositoryImpl;
import com.relic.data.models.PostModel;
import com.relic.data.models.SubredditModel;
import com.relic.databinding.DisplaySubBinding;
import com.relic.presentation.adapter.PostItemAdapter;
import com.relic.presentation.adapter.PostItemOnclick;
import com.relic.presentation.displaypost.DisplayPostView;

import java.util.List;


public class DisplaySubView extends Fragment {
  private final String TAG = "DISPLAYSUB_VIEW";
  private final String SCROLL_POSITION = "POSITION";
  DisplaySubContract.ViewModel displaySubVM;

  private DisplaySubBinding displaySubBinding;
  private PostItemAdapter postAdapter;
  private SwipeRefreshLayout swipeRefresh;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // parse the SubredditModel from the Bundle
    SubredditModel subModel = this.getArguments().getParcelable("SubredditModel");

    if (subModel != null) {
      getActivity().setTitle(subModel.getSubName());
      // get the viewmodel and inject the dependencies into it
      displaySubVM = ViewModelProviders.of(this).get(DisplaySubVM.class);

      // initialize a new post repo and inject it into the viewmodel
      // initialization occurs for vm only when the view is first created
      displaySubVM.init(subModel, new PostRepositoryImpl(this.getContext()));
    }
    else {
      Toast.makeText(this.getContext(), "There was an issue loading this sub", Toast.LENGTH_SHORT).show();
    }
  }


  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    // initialize the databinding for the layout
    displaySubBinding = DataBindingUtil.inflate(inflater, R.layout.display_sub, container, false);
    getActivity().setTitle(displaySubVM.getSubName());

    // initialize the post item adapter and attach it to the autogenerated view class
    postAdapter = new PostItemAdapter(new OnClick());
    displaySubBinding.displayPostsRecyclerview.setAdapter(postAdapter);

    swipeRefresh = displaySubBinding.getRoot().findViewById(R.id.display_posts_swipeRefreshLayout);

    attachScrollListeners();
    return displaySubBinding.getRoot();
  }


  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    if (displaySubVM == null) {
      // fetch the viewmodel if the fragment survives a reconfiguration change
      displaySubVM = ViewModelProviders.of(this).get(DisplaySubVM.class);
    }

    subscribeToPosts();
    //Toast.makeText(this.getContext(), "Orientation changed", Toast.LENGTH_SHORT).show();

    // recreate saved instance
    if (savedInstanceState != null) {
      Integer position = savedInstanceState.getInt(SCROLL_POSITION);

      Log.d(TAG, position + " = previous position");
      // scroll to the previous position before reconfiguration change
      displaySubBinding.displayPostsRecyclerview.smoothScrollToPosition(position);
    }
  }


  private void subscribeToPosts() {
    // observe the livedata list contained in the viewmodel
    displaySubVM.getPosts().observe(this, new Observer<List<PostModel>>() {
      @Override
      public void onChanged(@Nullable List<PostModel> postModels) {
        if (postModels != null) {
          // tells VM to retrieve more posts if there are no posts currently stored for this sub
          if (postModels.size() == 0) {
            displaySubVM.retrieveMorePosts(true);
            Log.d(TAG, "Requesting more posts from vm");
          }
          else {
            swipeRefresh.setRefreshing(false);
            // update the view whenever the livedata changes
            Log.d(TAG, "SIZE " + postModels.size());
            postAdapter.setPostList(postModels);
          }
          displaySubBinding.executePendingBindings();
        }
      }
    });
  }


  /**
   * Attach the event listeners for scrolling within the recyclerview and swiperefreshlayout
   */
  public void attachScrollListeners() {
    displaySubBinding.displayPostsRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);

        // checks if the recyclerview can no longer scroll downwards
        if (!recyclerView.canScrollVertically(1)) {
          // fetch the next post listing
          displaySubVM.retrieveMorePosts(false);
          Log.d(TAG, "Bottom reached, more posts retrieved");
        }
      }
    });

    swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        // empties current items to show that it's being refreshed
        postAdapter.resetPostList();

        // refresh the listing for this sub
        displaySubVM.retrieveMorePosts(true);
        Log.d(TAG, "Top pulled, posts refreshed");
      }
    });
  }


  /**
   * Onclick class with method for the view to hook onto
   */
  class OnClick implements PostItemOnclick {
    public void onClick(String postId, String subreddit) {
      // create a new bundle for the post id
      Bundle bundle = new Bundle();
      bundle.putString("full_name", postId);
      bundle.putString("subreddit", subreddit);

      DisplayPostView postFrag = new DisplayPostView();
      postFrag.setArguments(bundle);

      getActivity().getSupportFragmentManager().beginTransaction()
          .replace(R.id.main_content_frame, postFrag).addToBackStack(TAG).commit();
    }
  }



  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);

    LinearLayoutManager manager =  (LinearLayoutManager) displaySubBinding.displayPostsRecyclerview.getLayoutManager();
    // put the first visible item position into the bundle to allow us to get back to it
    outState.putInt(SCROLL_POSITION, manager.findFirstCompletelyVisibleItemPosition());
    Log.d(TAG, "First position = " + manager.findFirstCompletelyVisibleItemPosition());
  }


}
