package com.relic.viewmodel;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.support.v4.app.Fragment;

import com.relic.data.CommentRepository;
import com.relic.data.ListingRepository;
import com.relic.data.PostRepository;
import com.relic.data.models.CommentModel;
import com.relic.presentation.callbacks.RetrieveNextListingCallback;
import com.relic.presentation.displaypost.DisplayPostContract;
import com.relic.presentation.displaypost.DisplayPostVM;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class DisplayPostViewModelTest {
  @Mock
  Activity activity;
  @Mock Fragment displayPostView;
  @Mock Observer<List<CommentModel>> mCommentObserver;

  @Mock ListingRepository mListingRepo;
  @Mock PostRepository mPostRepo;
  @Mock CommentRepository mCommentRepo;

  // Tells Mockito to create mocks based on annotation
  @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Test
  public void displayPostVM_EmptyCommentList_ReturnsSizeZero() {
    List test = new ArrayList<>();

    CommentRepository mockCommentRepo = new CommentRepository() {
      @Override
      public LiveData<List<CommentModel>> getComments(String postFullName) {
        MutableLiveData <List<CommentModel>> liveList = new MutableLiveData<>();
        liveList.setValue(test);
        return liveList;
      }

      @Override
      public void retrieveComments(String subName, String postFullname, String after) {}
    };

    DisplayPostContract.ViewModel testVM = ViewModelProviders.of(displayPostView).get(DisplayPostVM.class);
    // TODO mock up the repository + objects for testing
    testVM.init(mListingRepo, mPostRepo, mCommentRepo, "Subreddit", "YEET");
    testVM.getCommentList().observeForever(mCommentObserver);
    testVM.retrieveMoreComments(false);

    //verify(mCommentObserver).onChanged();
    Mockito.verify(mCommentObserver).onChanged(test);
  }


}
