package com.relic.presentation.displaypost;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.NestedScrollingParent2;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;


/**
 * Custom nested scrollview - Overrides recyclerview events for:
 * 1) Scrolling downwards when the recyclerview reaches the top of its content
 * 2) Scrolling upwards when the recyclerview reaches the bottom of its content
 */
public class DisplayPostNestedScrollView extends NestedScrollView  {


  public DisplayPostNestedScrollView(@NonNull Context context) {
    super(context);
  }

  public DisplayPostNestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public DisplayPostNestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  /**
   * Checks if given nested scroll view has been scrolled to the bottom
   * @param nsv nested scroll view to check
   * @return whether the nsv has been scrolled to the bottom
   */
  private boolean isNsvScrolledToBottom (NestedScrollView nsv) {
    return !nsv.canScrollVertically(1);
  }

  private boolean isRvScrolledToTop (RecyclerView rv) {
    LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();
    int firstVisible = lm.findFirstVisibleItemPosition();
    int topItem = lm.findViewByPosition(0).getTop() ;

    return firstVisible == 0 && topItem ==0;
  }

  @Override
  public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
    RecyclerView rv = (RecyclerView) target;
    if ((dy < 0 && isRvScrolledToTop(rv)) || (dy > 0 && !isNsvScrolledToBottom(this))) {
      scrollBy(0, dy);
      consumed[1] = dy;
    }
    else {
      super.onNestedPreScroll(target, dx, dy, consumed);
    }
  }

  @Override
  public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
    boolean fling = true;
    RecyclerView rv = (RecyclerView) target;
    if ((velocityY < 0 && isRvScrolledToTop(rv)) || (velocityY > 0 && isNsvScrolledToBottom(this))) {
      fling((int) velocityY);
    }
    else {
      fling = super.onNestedPreFling(target, velocityX, velocityY);
    }
    return fling;
  }
}
