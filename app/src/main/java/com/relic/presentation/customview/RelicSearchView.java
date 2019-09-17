package com.relic.presentation.customview;

import android.content.Context;

import androidx.annotation.Nullable;
import android.text.Editable;
import android.text.method.KeyListener;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.relic.R;

public class RelicSearchView extends RelativeLayout {
  private final String TAG = "RELIC_SEARCHVIEW";
  boolean searchExpanded = false;

  private RelicSearchView relicSearchView;
  private EditText searchInput;


  public RelicSearchView(Context context) {
    super(context);
    relicSearchView = (RelicSearchView) inflate(getContext(), R.layout.relic_searchview, this);
  }


  public RelicSearchView(Context context, AttributeSet attributeSet) {
    super(context, attributeSet);
    relicSearchView = (RelicSearchView) inflate(getContext(), R.layout.relic_searchview, this);
  }


  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();

    searchInput = relicSearchView.findViewById(R.id.searchbox_edittext);

    // add onclick to show edit text
    findViewById(R.id.search_icon).setOnClickListener((View onClickView) -> {
      expandSearch(!searchExpanded);
      searchExpanded = !searchExpanded;
    });

    searchInput.setOnKeyListener(new OnKeyListener() {
      @Override
      public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
//        /Toast.makeText(getContext().getApplicationContext(), "Button pressed " + keyCode + " " + keyEvent.getAction(), Toast.LENGTH_SHORT).show();
        if (keyEvent.getAction() == KeyEvent.ACTION_UP && (keyCode == KeyEvent.ACTION_DOWN || keyCode == KeyEvent.KEYCODE_BACK)) {
          expandSearch(!searchExpanded);
          searchExpanded = !searchExpanded;
        }
        return false;
      }
    });

    searchInput.setKeyListener(new KeyListener() {
      @Override
      public int getInputType() {
        Toast.makeText(getContext().getApplicationContext(), "Button pressed ", Toast.LENGTH_SHORT).show();
        return 0;
      }

      @Override
      public boolean onKeyDown(View view, Editable editable, int i, KeyEvent keyEvent) {
        Toast.makeText(getContext().getApplicationContext(), "Button down ", Toast.LENGTH_SHORT).show();
        return false;
      }

      @Override
      public boolean onKeyUp(View view, Editable editable, int i, KeyEvent keyEvent) {
        Toast.makeText(getContext().getApplicationContext(), "Button up ", Toast.LENGTH_SHORT).show();
        return false;
      }

      @Override
      public boolean onKeyOther(View view, Editable editable, KeyEvent keyEvent) {
        Toast.makeText(getContext().getApplicationContext(), "Button other ", Toast.LENGTH_SHORT).show();
        return false;
      }

      @Override
      public void clearMetaKeyState(View view, Editable editable, int i) {
        Toast.makeText(getContext().getApplicationContext(), "Button meta ", Toast.LENGTH_SHORT).show();
      }
    });


    searchInput.setOnFocusChangeListener(new OnFocusChangeListener() {
      @Override
      public void onFocusChange(View view, boolean b) {
        Toast.makeText(getContext(), "Button pressed " + b, Toast.LENGTH_SHORT).show();
      }
    });

  }


  @Override
  protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
    super.onLayout(b, i, i1, i2, i3);
  }


  public void expandSearch (boolean expand) {
    int searchBox =  View.GONE;
    int searchIcon = View.VISIBLE;

    if (expand) {
      searchBox = View.VISIBLE;
      searchIcon = View.INVISIBLE;

      // retrieve reference to the search input and focus it
      searchInput.setFocusable(true);
      searchInput.requestFocus();
      InputMethodManager imm = (InputMethodManager) relicSearchView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT);
    }

    relicSearchView.findViewById(R.id.search_icon).setVisibility(searchIcon);
    relicSearchView.findViewById(R.id.search_box).setVisibility(searchBox);
  }


}
