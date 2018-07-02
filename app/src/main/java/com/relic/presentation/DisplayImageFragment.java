package com.relic.presentation;

import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.relic.R;
import com.relic.databinding.DisplayImageBinding;
import com.squareup.picasso.Picasso;

public class DisplayImageFragment extends Fragment {
  private final String IMAGE_KEY = "image_url";
  private DisplayImageBinding displayImageBinding;
  private String imageUrl;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // retrieve the image url from the bundled args
    imageUrl = getArguments().getString(IMAGE_KEY);
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);

    // retrieve the reference to the binding
    displayImageBinding = DataBindingUtil.inflate(inflater, R.layout.display_image, container, false);
    loadImage(container.getWidth());
    return displayImageBinding.getRoot();
  }


  private void loadImage(int parentWidth) {
    if (imageUrl != null) {
      Picasso.get().load(imageUrl).resize(parentWidth, 0).into(displayImageBinding.displayimageImageview);
    }
  }
}
