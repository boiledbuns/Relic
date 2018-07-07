package com.relic.viewmodel;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.support.v4.app.Fragment;

import com.relic.data.CommentRepository;
import com.relic.data.ListingRepository;
import com.relic.data.PostRepository;
import com.relic.data.models.CommentModel;
import com.relic.presentation.displaypost.DisplayPostContract;
import com.relic.presentation.displaypost.DisplayPostVM;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.List;

import static org.mockito.Mockito.verify;

public class DisplayPostVMTest {
  @Mock Fragment displayPostView;
  @Mock Observer<List<CommentModel>> mCommentObserver;

  @Mock ListingRepository mListingRepo;
  @Mock PostRepository mPostRepo;
  @Mock CommentRepository mCommentRepo;

  // Tells Mockito to create mocks based on annotation
  @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Test
  public void displayPostVM_EmptyCommentList_ReturnsSizeZero() {
    DisplayPostContract.ViewModel testVM = ViewModelProviders.of(displayPostView).get(DisplayPostVM.class);
    // TODO mock up the repository + objects for testing
    testVM.init(mListingRepo, mPostRepo, mCommentRepo, "Subreddit", "YEET");
    testVM.getCommentList().observeForever(mCommentObserver);
    testVM.retrieveMoreComments(false);

    //verify(mCommentObserver).onChanged();
  }


}
